package restx.annotations.processor;

import static restx.annotations.processor.TypeHelper.getTypeExpressionFor;
import static restx.annotations.processor.TypeHelper.toTypeDescription;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.samskivert.mustache.Template;

import restx.RestxLogLevel;
import restx.StdRestxRequestMatcher;
import restx.annotations.Annotations;
import restx.annotations.Consumes;
import restx.annotations.ContextParam;
import restx.annotations.DELETE;
import restx.annotations.GET;
import restx.annotations.HEAD;
import restx.annotations.HeaderParam;
import restx.annotations.POST;
import restx.annotations.PUT;
import restx.annotations.Param;
import restx.annotations.PathParam;
import restx.annotations.Produces;
import restx.annotations.QueryParam;
import restx.annotations.RestxResource;
import restx.annotations.SuccessStatus;
import restx.annotations.Verbosity;
import restx.common.Mustaches;
import restx.common.processor.RestxAbstractProcessor;
import restx.endpoint.EndpointParameterKind;
import restx.factory.When;
import restx.http.HttpStatus;
import restx.security.PermitAll;
import restx.security.RolesAllowed;
import restx.validation.ValidatedFor;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 10:02 PM
 */
@SupportedAnnotationTypes({
        "restx.annotations.RestxResource"
})
@SupportedOptions({ "debug" })
public class RestxAnnotationProcessor extends RestxAbstractProcessor {
    private static final Pattern ROLE_PARAM_INTERPOLATOR_REGEX = Pattern.compile("\\{(.+?)\\}");

    final Template routerTpl;

    private static final Function<Class,String> FQN_EXTRACTOR = new Function<Class,String>(){
        @Override
        public String apply(Class clazz) {
            return clazz.getCanonicalName();
        }
    };

    public RestxAnnotationProcessor() {
        routerTpl = Mustaches.compile(RestxAnnotationProcessor.class, "RestxRouter.mustache");
    }

    @Override
    protected boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception {
        final Map<String, ResourceGroup> groups = Maps.newHashMap();
        final Set<Element> modulesListOriginatingElements = Sets.newHashSet();

        for (ResourceMethodAnnotation annotation : getResourceMethodAnnotationsInRound(roundEnv)) {
            try {
                TypeElement typeElem = (TypeElement) annotation.methodElem.getEnclosingElement();
                ResourceClassDef r = getResourceClassDef(typeElem);
                if (r == null) {
                    error(
                        String.format("%s rest method found - enclosing class %s must be annotated with @RestxResource",
                                annotation.methodElem.getSimpleName(), typeElem.getSimpleName()), typeElem);
                    continue;
                }

                SuccessStatus successStatusAnn = annotation.methodElem.getAnnotation(SuccessStatus.class);
                HttpStatus successStatus = successStatusAnn==null?HttpStatus.OK:successStatusAnn.value();

                Verbosity verbosity = annotation.methodElem.getAnnotation(Verbosity.class);
                RestxLogLevel logLevel = verbosity == null ? RestxLogLevel.DEFAULT : verbosity.value();

                ResourceGroup group = getResourceGroup(r, groups);
                ResourceClass resourceClass = getResourceClass(typeElem, r, group, modulesListOriginatingElements);

                String permission = buildPermission(annotation, typeElem);

                Consumes inContentTypeAnn = annotation.methodElem.getAnnotation(Consumes.class);
                Optional<String> inContentType = Optional.fromNullable(inContentTypeAnn != null ? inContentTypeAnn.value() : null);
                Produces outContentTypeAnn = annotation.methodElem.getAnnotation(Produces.class);
                Optional<String> outContentType = Optional.fromNullable(outContentTypeAnn != null ? outContentTypeAnn.value() : null);

                ImmutableList.Builder<AnnotationDescription> annotationDescriptionsBuilder = ImmutableList.builder();
                for(AnnotationMirror methodAnnotation: annotation.methodElem.getAnnotationMirrors()) {
                    AnnotationDescription annotationDescription = createAnnotationDescriptionFrom(methodAnnotation, annotation.methodElem);
                    annotationDescriptionsBuilder.add(annotationDescription);
                }

                ResourceMethod resourceMethod = new ResourceMethod(
                        resourceClass,
                        annotation.httpMethod, r.value + annotation.path,
                        annotation.methodElem.getSimpleName().toString(),
                        annotation.methodElem.getReturnType().toString(),
                        annotation.methodElem.getThrownTypes().toString(),
                        successStatus, logLevel, permission,
                        typeElem.getQualifiedName().toString() + "#" + annotation.methodElem.toString(),
                        inContentType, outContentType, annotationDescriptionsBuilder.build()
                );

                resourceClass.resourceMethods.add(resourceMethod);
                resourceClass.originatingElements.add(annotation.methodElem);

                buildResourceMethodParams(annotation, resourceMethod);
            } catch (Exception e) {
                fatalError("error when processing " + annotation.methodElem, e, annotation.methodElem);
            }
        }

        if (!groups.isEmpty()) {
            generateFiles(groups, modulesListOriginatingElements);
        }
        return true;
    }

    private enum AnnotationFieldKind {
        PRIMITIVE() {
            @Override
            public String transformSingleValueToExpression(Object value, AnnotationField annotationField) {
                String val = super.transformSingleValueToExpression(value, annotationField);
                switch(annotationField.type.toString()) {
                    case "float": return val+"f";
                    case "char": return "'"+val+"'";
                    default: return val;
                }
            }
        }, STRING() {
            @Override
            public String transformSingleValueToExpression(Object value, AnnotationField annotationField) {
                return "\"" + super.transformSingleValueToExpression(value, annotationField).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            }
        }, CLASS() {
            @Override
            public String transformSingleValueToExpression(Object value, AnnotationField annotationField) {
                return super.transformSingleValueToExpression(value, annotationField) + ".class";
            }
        }, ENUM() {
            @Override
            public String transformSingleValueToExpression(Object value, AnnotationField annotationField) {
                return annotationField.type+"."+value.toString();
            }
        }, ANNOTATION() {
            @Override
            public String transformSingleValueToExpression(Object value, AnnotationField annotationField) {
                return annotationField.type+"."+value.toString();
            }
        };

        public static boolean isArrayed(TypeMirror type) {
            return TypeKind.ARRAY.equals(type.getKind());
        }

        public static TypeMirror componentTypeOf(TypeMirror type) {
            return (TypeKind.ARRAY.equals(type.getKind()))?((ArrayType) type).getComponentType():type;
        }

        public static AnnotationFieldKind valueOf(ProcessingEnvironment processingEnv, TypeMirror type) {
            TypeMirror componentType = componentTypeOf(type);

            if(componentType.getKind().isPrimitive()) {
                return PRIMITIVE;
            } else if(String.class.getCanonicalName().equals(componentType.toString())) {
                return STRING;
            } else if(Class.class.getCanonicalName().equals(TypeHelper.rawTypeFrom(componentType.toString()))) {
                return CLASS;
            } else {
                ImmutableList<String> superTypesClassNames = FluentIterable.from(processingEnv.getTypeUtils().directSupertypes(componentType))
                        .transform(Functions.toStringFunction())
                        .toList();
                if(superTypesClassNames.contains(Annotation.class.getCanonicalName())) {
                    return ANNOTATION;
                } else {
                    return ENUM;
                }
            }
        }

        public String transformSingleValueToExpression(Object value, AnnotationField annotationField) {
            return value.toString();
        }
    }

    private AnnotationDescription createAnnotationDescriptionFrom(AnnotationMirror methodAnnotation,
            ExecutableElement element) {
        ImmutableList.Builder<AnnotationField> annotationFieldsBuilder = ImmutableList.builder();
        ImmutableSet.Builder<String> annotationFieldNamesBuilder = ImmutableSet.builder();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> fieldEntry : methodAnnotation
                .getElementValues().entrySet()) {
            String fieldName = fieldEntry.getKey().getSimpleName().toString();
//            .toString().substring(0,
//                    fieldEntry.getKey().toString().length() - "()".length());
            TypeMirror type = fieldEntry.getKey().getReturnType();
            TypeMirror componentType = AnnotationFieldKind.componentTypeOf(type);
            boolean arrayed = AnnotationFieldKind.isArrayed(type);
            AnnotationFieldKind annotationFieldKind = AnnotationFieldKind.valueOf(processingEnv, type);

            annotationFieldsBuilder.add(new AnnotationField(fieldName, fieldEntry.getValue().getValue(), componentType,
                    annotationFieldKind, arrayed));
            annotationFieldNamesBuilder.add(fieldName);
        }

        // Filling annotation default values (not provided in annotation
        // declaration)
        ImmutableSet<String> declaredAnnotationFieldNames = annotationFieldNamesBuilder.build();
        for (Element annotationMember : methodAnnotation.getAnnotationType().asElement().getEnclosedElements()) {
            if (annotationMember.getKind() == ElementKind.METHOD) {
                ExecutableElement annotationMemberAsMethod = (ExecutableElement) annotationMember;
                String fieldName = annotationMemberAsMethod.getSimpleName().toString();
                if (!declaredAnnotationFieldNames.contains(fieldName)) {
                    TypeMirror type = annotationMemberAsMethod.getReturnType();
                    TypeMirror componentType = AnnotationFieldKind.componentTypeOf(type);
                    boolean arrayed = AnnotationFieldKind.isArrayed(type);
                    AnnotationFieldKind annotationFieldKind = AnnotationFieldKind.valueOf(processingEnv, type);

                    annotationFieldsBuilder.add(new AnnotationField(fieldName,
                            annotationMemberAsMethod.getDefaultValue() == null ? null
                                    : annotationMemberAsMethod.getDefaultValue().getValue(),
                            componentType, annotationFieldKind, arrayed));
                }
            }
        }

        AnnotationDescription annotationDescription = new AnnotationDescription(
                methodAnnotation.getAnnotationType().toString(), annotationFieldsBuilder.build());
        return annotationDescription;
    }

    protected ResourceClassDef getResourceClassDef(TypeElement typeElem) {
        RestxResource r = typeElem.getAnnotation(RestxResource.class);
        ResourceClassDef resourceClassDef = new ResourceClassDef();
        resourceClassDef.value = r.value();
        resourceClassDef.priority = r.priority();
        resourceClassDef.group = r.group();
        return resourceClassDef;
    }

    protected Class<? extends Annotation> getRestAnnotationClass() {
        return RestxResource.class;
    }

    private String buildPermission(ResourceMethodAnnotation annotation, TypeElement typeElem) {
        String permission;
        PermitAll permitAll = annotation.methodElem.getAnnotation(PermitAll.class);
        if (permitAll != null) {
            permission = "open()";
        } else {
            RolesAllowed rolesAllowed = annotation.methodElem.getAnnotation(RolesAllowed.class);
            if (rolesAllowed != null) {
                List<String> roles = new ArrayList<>();
                for (String role : rolesAllowed.value()) {
                    for(String wildcardedRole : generateWildcardRolesFor(role)){
                        roles.add("hasRole(\"" + wildcardedRole + "\")");
                    }
                }
                switch (roles.size()) {
                    case 0:
                        permission = "isAuthenticated()";
                        break;
                    case 1:
                        permission = roles.get(0);
                        break;
                    default:
                        permission = "anyOf(" + Joiner.on(", ").join(roles) + ")";
                }
            } else {
                permitAll = typeElem.getAnnotation(PermitAll.class);
                if (permitAll != null) {
                    permission = "open()";
                } else {
                    permission = "isAuthenticated()";
                }
            }
        }
        return permission;
    }

    private static List<String> generateWildcardRolesFor(String role) {
        String[] roleChunks = Splitter.on(ROLE_PARAM_INTERPOLATOR_REGEX).splitToList(role).toArray(new String[0]);

        List<String> combinatorialInterpolatedRoles = new ArrayList<>();
        if(roleChunks.length == 1){
            combinatorialInterpolatedRoles.add(role);
        } else {
            List<ImmutableSet<String>> variablePotentialValues = new ArrayList<>();

            // Generating cartesian product with [variableName, "*"] on every variables in role
            // It will allow to check for wildcards on every variable chunk in role
            Matcher matcher = ROLE_PARAM_INTERPOLATOR_REGEX.matcher(role);
            while(matcher.find()) {
                String variableName = matcher.group(1);
                variablePotentialValues.add(ImmutableSet.of("*", "{"+variableName+"}"));
            }

            Set<List<String>> variableCombinations = Sets.cartesianProduct(variablePotentialValues);
            for(List<String> variableCombination : variableCombinations){
                StringBuilder interpolatedRole = new StringBuilder(roleChunks[0]);
                int i=1;
                for(String value : variableCombination){
                    interpolatedRole.append(value).append(roleChunks[i]);
                    i++;
                }

                combinatorialInterpolatedRoles.add(interpolatedRole.toString());
            }
        }

        return combinatorialInterpolatedRoles;
    }

    private Param createParam(final String value, final Param.Kind kind) {
        return new Param() {
            @Override
            public Class<? extends Annotation> annotationType() { return Param.class; }
            @Override
            public String value() { return value; }
            @Override
            public Kind kind() { return kind; }
        };
    }

    private void buildResourceMethodParams(ResourceMethodAnnotation annotation, ResourceMethod resourceMethod) {
        Set<String> pathParamNamesToMatch = Sets.newHashSet(resourceMethod.pathParamNames);
        for (VariableElement p : annotation.methodElem.getParameters()) {
            Param param = p.getAnnotation(Param.class);
            String variableName = p.getSimpleName().toString();
            ResourceMethodParameterKind parameterKind = null;
            String reqParamName;
            if (param == null) {
                final QueryParam queryParamAnn = p.getAnnotation(QueryParam.class);
                final PathParam pathParamAnn = p.getAnnotation(PathParam.class);
                final ContextParam contextParamAnn = p.getAnnotation(ContextParam.class);
                final HeaderParam reqHeaderAnn = p.getAnnotation(HeaderParam.class);
                if (queryParamAnn != null) {
                    param = createParam(queryParamAnn.value(), Param.Kind.QUERY);
                } else if (pathParamAnn != null) {
                    param = createParam(pathParamAnn.value(), Param.Kind.PATH);
                } else if (contextParamAnn != null) {
                    param = createParam(contextParamAnn.value(), Param.Kind.CONTEXT);
                } else if (reqHeaderAnn != null) {
                    param = createParam(reqHeaderAnn.value(), Param.Kind.HEADER);
                }
            }

            if(param != null) {
                reqParamName = param.value().length() == 0 ? variableName : param.value();
                if (param.kind() != Param.Kind.DEFAULT) {
                    parameterKind = ResourceMethodParameterKind.valueOf(param.kind().name());
                }
            } else {
                reqParamName = variableName;
            }

            if (pathParamNamesToMatch.contains(reqParamName)) {
                if (parameterKind != null && parameterKind != ResourceMethodParameterKind.PATH) {
                    error(
                        String.format("%s param %s matches a Path param name", parameterKind.name(), reqParamName),
                            annotation.methodElem);
                    continue;
                }
                pathParamNamesToMatch.remove(reqParamName);
                parameterKind = ResourceMethodParameterKind.PATH;
            } else if (parameterKind == null) {
                if (ImmutableList.of("GET", "HEAD", "DELETE").contains(resourceMethod.httpMethod)) {
                    parameterKind = ResourceMethodParameterKind.QUERY;
                } else {
                    parameterKind = ResourceMethodParameterKind.BODY;
                }
            }

            ValidatedFor validatedFor = p.getAnnotation(ValidatedFor.class);

            String[] validationGroups = new String[0];
            if(validatedFor != null) {
                validationGroups = Annotations.getAnnotationClassValuesAsFQCN(p, ValidatedFor.class, "value");
            }

            resourceMethod.parameters.add(new ResourceMethodParameter(
                p.asType().toString(),
                variableName,
                reqParamName,
                parameterKind,
                validationGroups));
        }
        if (!pathParamNamesToMatch.isEmpty()) {
            error(
                String.format("path param(s) %s not found among method parameters", pathParamNamesToMatch),
                    annotation.methodElem);
        }
    }

    private ResourceGroup getResourceGroup(ResourceClassDef r, Map<String, ResourceGroup> groups) {
        ResourceGroup group = groups.get(r.group);
        if (group == null) {
            groups.put(r.group, group = new ResourceGroup(r.group));
        }
        return group;
    }

    private ResourceClass getResourceClass(TypeElement typeElem, ResourceClassDef r, ResourceGroup group, Set<Element> modulesListOriginatingElements) {
        String fqcn = typeElem.getQualifiedName().toString();
        ResourceClass resourceClass = group.resourceClasses.get(fqcn);
        if (resourceClass == null) {
            modulesListOriginatingElements.add(typeElem);
            When when = typeElem.getAnnotation(When.class);
            group.resourceClasses.put(fqcn, resourceClass = new ResourceClass(group, fqcn, r.priority,
                    when == null ? ""
                            : ("@restx.factory.When(name=\"" + when.name() + "\", value=\"" + when.value() + "\")")));
            resourceClass.originatingElements.add(typeElem);
        }
        return resourceClass;
    }

    private void generateFiles(Map<String, ResourceGroup> groups, Set<Element> modulesListOriginatingElements) throws IOException {
        for (ResourceGroup group : groups.values()) {
            for (ResourceClass resourceClass: group.resourceClasses.values()) {
                List<ImmutableMap<String, Object>> routes = Lists.newArrayList();

                buildResourceRoutesCodeChunks(resourceClass, routes);

                ImmutableMap<String, Object> ctx = ImmutableMap.<String, Object>builder()
                        .put("package", resourceClass.pack)
                        .put("routerGroup", group.name)
                        .put("router", resourceClass.name + "Router")
                        .put("resource", resourceClass.name)
                        .put("priority", resourceClass.priority)
                        .put("condition", resourceClass.condition)
                        .put("routes", routes)
                        .build();

                generateJavaClass(resourceClass.fqcn + "Router", routerTpl, ctx, resourceClass.originatingElements);
            }
        }
    }

    private void buildResourceRoutesCodeChunks(ResourceClass resourceClass, List<ImmutableMap<String, Object>> routes) {
        for (ResourceMethod resourceMethod : resourceClass.resourceMethods) {

            List<String> callParameters = Lists.newArrayList();
            List<String> parametersDescription = Lists.newArrayList();
            List<String> queryParametersDefinition = Lists.newArrayList();

            String inEntityClass = "Void";
            for (ResourceMethodParameter parameter : resourceMethod.parameters) {
                // TODO: Simplify this since it seems useless for now...
                ResourceMethodParameterKind kind = parameter.kind;
                if (!String.class.getName().equals(parameter.type)
                        && kind != ResourceMethodParameterKind.BODY
                        && kind != ResourceMethodParameterKind.CONTEXT) {
                    // Do nothing
                } else if (kind == ResourceMethodParameterKind.BODY) {
                    inEntityClass = parameter.type;
                }
                callParameters.add(String.format("/* [%s] %s */ %s", kind, parameter.reqParamName, kind.fetchFromReqCode(parameter, resourceMethod)));

                if(kind.resolvedWithQueryParamMapper()) {
                    queryParametersDefinition.add(String.format("                    ParamDef.of(%s, \"%s\")",
                            TypeHelper.getTypeReferenceExpressionFor(parameter.type), parameter.reqParamName));
                }

                if (kind != ResourceMethodParameterKind.CONTEXT) {
                    parametersDescription.add(String.format(
                            "                OperationParameterDescription {PARAMETER} = new OperationParameterDescription();\n" +
                            "                {PARAMETER}.name = \"%s\";\n" +
                            "                {PARAMETER}.paramType = OperationParameterDescription.ParamType.%s;\n" +
                            "                {PARAMETER}.dataType = \"%s\";\n" +
                            "                {PARAMETER}.schemaKey = \"%s\";\n" +
                            "                {PARAMETER}.required = %s;\n" +
                            "                operation.parameters.add({PARAMETER});\n",
                            parameter.reqParamName,
                            kind.name().toLowerCase(),
                            toTypeDescription(parameter.type),
                            toSchemaKey(parameter.type),
                            String.valueOf(!parameter.guavaOptional && !parameter.java8Optional)
                    ).replaceAll("\\{PARAMETER}", parameter.name));
                }
            }

            String call = "resource." + resourceMethod.name + "(\n" +
                    "                        " +
                    Joiner.on(",\n                        ").join(callParameters) + "\n" +
                    "                    )";

            if (resourceMethod.returnType.equalsIgnoreCase("void")) {
                call = call + ";\n" +
                        "                    return Optional.of(Empty.EMPTY);";
            } else {
                if (resourceMethod.returnTypeGuavaOptional) {
                    call = call ;
                } else if (resourceMethod.returnTypeJava8Optional) {
                    call = "Optional.fromNullable(" + call + ".orElse(null))";
                } else {
                    call = "Optional.of(" + call + ")";
                }
                call = "return " + call + ";";

            }

            String outEntity = resourceMethod.returnType.equalsIgnoreCase("void") ? "Empty" : resourceMethod.returnType;
            routes.add(ImmutableMap.<String, Object>builder()
                    .put("routeId", resourceMethod.id)
                    .put("routeName", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, resourceMethod.name))
                    .put("method", resourceMethod.httpMethod)
                    .put("path", resourceMethod.path.replace("\\", "\\\\"))
                    .put("resource", resourceClass.name)
                    .put("securityCheck", "securityManager.check(request, match, " + resourceMethod.permission + ");")
                    .put("queryParametersDefinition", Joiner.on(",\n").join(queryParametersDefinition))
                    .put("throwsIOException", resourceMethod.throwsIOException())
                    .put("call", call)
                    .put("responseClass", toTypeDescription(resourceMethod.returnType))
                    .put("sourceLocation", resourceMethod.sourceLocation)
                    .put("parametersDescription", Joiner.on("\n").join(parametersDescription))
                    .put("annotationDescriptions", resourceMethod.annotationDescriptions)
                    .put("inEntity", inEntityClass)
                    .put("inEntityType", getTypeExpressionFor(inEntityClass))
                    .put("inEntitySchemaKey", toSchemaKey(inEntityClass))
                    .put("outEntity", outEntity)
                    .put("outEntityType", getTypeExpressionFor(resourceMethod.returnType))
                    .put("outEntitySchemaKey", toSchemaKey(resourceMethod.returnType))
                    .put("inContentType",
                            resourceMethod.inContentType.isPresent() ?
                                    String.format("Optional.of(\"%s\")", resourceMethod.inContentType.get()) : "Optional.<String>absent()")
                    .put("outContentType",
                            resourceMethod.outContentType.isPresent() ?
                                    String.format("Optional.of(\"%s\")", resourceMethod.outContentType.get()) : "Optional.<String>absent()")
                    .put("successStatusName", resourceMethod.successStatus.name())
                    .put("logLevelName", resourceMethod.logLevel.name())
                    .build()
            );
        }
    }


    private String toSchemaKey(String type) {
        Pattern p = Pattern.compile("java\\.lang\\.Iterable<(.+)>");
        Matcher m = p.matcher(type);
        if (m.matches()) {
            type =  m.group(1);
        }
        if (type.startsWith("java.lang")
            || type.startsWith("java.util")
            || type.equalsIgnoreCase("void")
                ) {
            return "";
        } else {
            return type;
        }
    }

    private Collection<ResourceMethodAnnotation> getResourceMethodAnnotationsInRound(RoundEnvironment roundEnv) {
        Collection<ResourceMethodAnnotation> methodAnnotations = Lists.newArrayList();
        for (Element resourceElem : roundEnv.getElementsAnnotatedWith(getRestAnnotationClass())) {
            if (! (resourceElem instanceof TypeElement)) {
                error(
                    String.format("Only a class can be annotated with @RestxResource. Found %s",
                            resourceElem.getSimpleName()), resourceElem);
                continue;
            }

            for (Element elem : resourceElem.getEnclosedElements()) {
                if (elem.getKind() == ElementKind.METHOD) {
                    // iterating through these annotations would be nicer, but we would need to use reflection for "value()"
                    GET get = elem.getAnnotation(GET.class);
                    if (get != null) {
                        methodAnnotations.add(new ResourceMethodAnnotation("GET", elem, get.value()));
                    }
                    POST post = elem.getAnnotation(POST.class);
                    if (post != null) {
                        methodAnnotations.add(new ResourceMethodAnnotation("POST", elem, post.value()));
                    }
                    PUT put = elem.getAnnotation(PUT.class);
                    if (put != null) {
                        methodAnnotations.add(new ResourceMethodAnnotation("PUT", elem, put.value()));
                    }
                    DELETE delete = elem.getAnnotation(DELETE.class);
                    if (delete != null) {
                        methodAnnotations.add(new ResourceMethodAnnotation("DELETE", elem, delete.value()));
                    }
                    HEAD head = elem.getAnnotation(HEAD.class);
                    if (head != null) {
                        methodAnnotations.add(new ResourceMethodAnnotation("HEAD", elem, head.value()));
                    }
                }
            }
        }
        return methodAnnotations;
    }

    private static class ResourceMethodAnnotation {
        final String httpMethod;
        final ExecutableElement methodElem;
        final String path;

        private ResourceMethodAnnotation(String httpMethod, Element methodElem, String path) {
            this.httpMethod = httpMethod;
            this.methodElem = (ExecutableElement) methodElem;
            this.path = path;
        }
    }

    private static class ResourceGroup {
        final String name;
        final Map<String, ResourceClass> resourceClasses = Maps.newLinkedHashMap();

        ResourceGroup(String name) {
            this.name = name;
        }
    }

    public static class ResourceClassDef {
        public String value;
        public String group;
        public int priority;
    }

    private static class ResourceClass {
        final String pack;
        final String fqcn;
        final int priority;
        final List<ResourceMethod> resourceMethods = Lists.newArrayList();
        final ResourceGroup group;
        final String name;
        final String condition;
        final Set<Element> originatingElements = Sets.newHashSet();

        ResourceClass(ResourceGroup group, String fqcn, int priority, String condition) {
            this.group = group;
            this.fqcn = fqcn;
            this.priority = priority;
            this.condition = condition;
            this.pack = fqcn.substring(0, fqcn.lastIndexOf('.'));
            this.name = fqcn.substring(fqcn.lastIndexOf('.') + 1);
        }
    }

    private static class AnnotationField {
        final String name;
        final Object value;
        final TypeMirror type;
        final AnnotationFieldKind kind;
        final boolean isArray;

        public AnnotationField(String name, Object value, TypeMirror type, AnnotationFieldKind kind, boolean isArray) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.kind = kind;
            this.isArray = isArray;
        }

        String getValueCodeInstanciation() {
            if(AnnotationFieldKind.ANNOTATION.equals(this.kind)) {
                return "throw new java.lang.UnsupportedOperationException(\"Unsupported annotation field type\")";
            } else if(isArray) {
                return String.format("return new %s[]{ %s }",
                        // Arrays cannot be parameterized
                        TypeHelper.rawTypeFrom(type.toString()),
                        Joiner.on(", ").join((List)value));
            } else {
                return "return "+kind.transformSingleValueToExpression(value, this);
            }
        }
    }

    private static class AnnotationDescription {
        final String annotationClass;
        final ImmutableList<AnnotationField> annotationFields;

        public AnnotationDescription(String annotationClass, ImmutableList<AnnotationField> annotationFields) {
            this.annotationClass = annotationClass;
            this.annotationFields = annotationFields;
        }
    }

    private static class ResourceMethod {
        final String httpMethod;
        final String path;
        final String name;
        final String realReturnType;
        final boolean returnTypeGuavaOptional;
        final boolean returnTypeJava8Optional;
        final String returnType;
        final String thrownTypes;
        final String id;
        final ImmutableList<String> pathParamNames;
        final HttpStatus successStatus;
        final RestxLogLevel logLevel;
        final String permission;
        final String sourceLocation;
        final Optional<String> inContentType;
        final Optional<String> outContentType;
        final ImmutableList<AnnotationDescription> annotationDescriptions;

        final List<ResourceMethodParameter> parameters = Lists.newArrayList();

        ResourceMethod(ResourceClass resourceClass, String httpMethod, String path, String name, String returnType,
                       String thrownTypes, HttpStatus successStatus, RestxLogLevel logLevel, String permission,
                       String sourceLocation, Optional<String> inContentType, Optional<String> outContentType,
                       ImmutableList<AnnotationDescription> annotationDescriptions) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.name = name;
            this.logLevel = logLevel;
            this.permission = permission;
            this.sourceLocation = sourceLocation;
            this.inContentType = inContentType;
            this.outContentType = outContentType;

            this.thrownTypes = thrownTypes;
            this.realReturnType = returnType;

            TypeHelper.OptionalMatchingType optionalMatchingReturnType = TypeHelper.optionalMatchingTypeOf(returnType);
            if(optionalMatchingReturnType.getOptionalType() == TypeHelper.OptionalMatchingType.Type.GUAVA) {
                this.returnTypeGuavaOptional = true;
                this.returnTypeJava8Optional = false;
            } else if(optionalMatchingReturnType.getOptionalType() == TypeHelper.OptionalMatchingType.Type.JAVA8) {
                this.returnTypeGuavaOptional = false;
                this.returnTypeJava8Optional = true;
            } else {
                this.returnTypeGuavaOptional = false;
                this.returnTypeJava8Optional = false;
            }
            this.returnType = optionalMatchingReturnType.getUnderlyingType();

            this.id = resourceClass.group.name + "#" + resourceClass.name + "#" + name;
            this.successStatus = successStatus;
            StdRestxRequestMatcher requestMatcher = new StdRestxRequestMatcher(httpMethod, path);
            pathParamNames = requestMatcher.getPathParamNames();

            this.annotationDescriptions = annotationDescriptions;
        }

        boolean throwsIOException() {
            return thrownTypes != null && thrownTypes.contains(IOException.class.getCanonicalName());
        }
    }

    static class ResourceMethodParameter {
        final String type;
        final String realType;
        final boolean guavaOptional;
        final boolean java8Optional;
        final String name;
        final String reqParamName;
        final ResourceMethodParameterKind kind;
        final List<String> validationGroupsFQNs;

        private static final Function<String, String> CLASS_APPENDER_FCT = new Function<String, String>() {
            @Override
            public String apply(String fqn) {
                return fqn+".class";
            }
        };

        private ResourceMethodParameter(String type, String name, String reqParamName, ResourceMethodParameterKind kind, String[] validationGroupsFQNs) {
            TypeHelper.OptionalMatchingType optionalMatchingReturnType = TypeHelper.optionalMatchingTypeOf(type);
            if(optionalMatchingReturnType.getOptionalType() == TypeHelper.OptionalMatchingType.Type.GUAVA) {
                this.guavaOptional = true;
                this.java8Optional = false;
            } else if(optionalMatchingReturnType.getOptionalType() == TypeHelper.OptionalMatchingType.Type.JAVA8) {
                this.guavaOptional = false;
                this.java8Optional = true;
            } else {
                this.guavaOptional = false;
                this.java8Optional = false;
            }
            this.type = optionalMatchingReturnType.getUnderlyingType();
            this.realType = type;

            this.name = name;
            this.reqParamName = reqParamName;
            this.kind = kind;
            this.validationGroupsFQNs = Arrays.asList(validationGroupsFQNs);
        }

        public Optional<String> joinedValidationGroupFQNExpression(){
            if(this.validationGroupsFQNs == null || this.validationGroupsFQNs.isEmpty()) {
                return Optional.absent();
            } else {
                return Optional.of(Joiner.on(", ").join(Iterables.transform(this.validationGroupsFQNs, CLASS_APPENDER_FCT)));
            }
        }
    }

    private static enum ResourceMethodParameterKind {
        QUERY(true) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                return ParameterExpressionBuilder
                        .createFromMapQueryObjectFromRequest(parameter, EndpointParameterKind.QUERY)
                        .surroundWithCheckValid(parameter)
                        .getParameterExpr();
            }
        },
        PATH(true) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                return ParameterExpressionBuilder
                        .createFromMapQueryObjectFromRequest(parameter, EndpointParameterKind.PATH)
                        .surroundWithCheckValid(parameter)
                        .getParameterExpr();
            }
        },
        HEADER(true) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                return ParameterExpressionBuilder
                        .createFromMapQueryObjectFromRequest(parameter, EndpointParameterKind.HEADER)
                        .surroundWithCheckValid(parameter)
                        .getParameterExpr();
            }
        },
        BODY(false) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                return ParameterExpressionBuilder
                        .createFromExpr("body", "body")
                        .surroundWithCheckValid(parameter)
                        .getParameterExpr();
            }
        },
        CONTEXT(false) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                Collection<String> contextParamNames = Arrays.asList("baseUri", "clientAddress", "request", "response", "locale", "locales");
                if (!contextParamNames.contains(parameter.reqParamName)) {
                    throw new IllegalArgumentException("context parameter not known: " + parameter.reqParamName +
                            ". Possible names are: " + Joiner.on(", ").join(contextParamNames));
                }
                switch (parameter.reqParamName) {
                    case "request":
                        return "request";
                    case "response":
                        return "response";
                    case "baseUri":
                        return "request.getBaseUri()";
                    case "clientAddress":
                        return "request.getClientAddress()";
                    case "locale":
                        return "request.getLocale()";
                    case "locales":
                        return "request.getLocales()";
                    default:
                        throw new IllegalStateException(
                                "invalid context param name not catched by contextParamNames list !! " + parameter.reqParamName);
                }
            }
        };

        private final boolean resolvedWithQueryParamMapper;

        ResourceMethodParameterKind(boolean resolvedWithQueryParamMapper) {
            this.resolvedWithQueryParamMapper = resolvedWithQueryParamMapper;
        }

        public abstract String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method);

        public boolean resolvedWithQueryParamMapper() {
            return resolvedWithQueryParamMapper;
        }
    }

}
