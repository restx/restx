package restx.annotations.processor;

import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.samskivert.mustache.Template;
import restx.RestxLogLevel;
import restx.StdRestxRequestMatcher;
import restx.annotations.*;
import restx.common.Mustaches;
import restx.common.processor.RestxAbstractProcessor;
import restx.endpoint.EndpointParameterKind;
import restx.factory.When;
import restx.http.HttpStatus;
import restx.security.PermitAll;
import restx.security.RolesAllowed;
import restx.validation.ValidatedFor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static restx.annotations.processor.TypeHelper.getTypeExpressionFor;

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
                RestxResource r = typeElem.getAnnotation(RestxResource.class);
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

                ResourceMethod resourceMethod = new ResourceMethod(
                        resourceClass,
                        annotation.httpMethod, r.value() + annotation.path,
                        annotation.methodElem.getSimpleName().toString(),
                        annotation.methodElem.getReturnType().toString(),
                        successStatus, logLevel, permission,
                        typeElem.getQualifiedName().toString() + "#" + annotation.methodElem.toString(),
                        inContentType, outContentType
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

    private void buildResourceMethodParams(ResourceMethodAnnotation annotation, ResourceMethod resourceMethod) {
        Set<String> pathParamNamesToMatch = Sets.newHashSet(resourceMethod.pathParamNames);
        for (VariableElement p : annotation.methodElem.getParameters()) {
            Param param = p.getAnnotation(Param.class);
            String paramName = p.getSimpleName().toString();
            String reqParamName = p.getSimpleName().toString();
            ResourceMethodParameterKind parameterKind = null;
            if (param != null) {
                reqParamName = param.value().length() == 0 ? paramName : param.value();
                if (param.kind() != Param.Kind.DEFAULT) {
                    parameterKind = ResourceMethodParameterKind.valueOf(param.kind().name());
                }
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
                if (ImmutableList.of("GET", "HEAD").contains(resourceMethod.httpMethod)) {
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
                paramName,
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

    private ResourceGroup getResourceGroup(RestxResource r, Map<String, ResourceGroup> groups) {
        ResourceGroup group = groups.get(r.group());
        if (group == null) {
            groups.put(r.group(), group = new ResourceGroup(r.group()));
        }
        return group;
    }

    private ResourceClass getResourceClass(TypeElement typeElem, RestxResource r, ResourceGroup group, Set<Element> modulesListOriginatingElements) {
        String fqcn = typeElem.getQualifiedName().toString();
        ResourceClass resourceClass = group.resourceClasses.get(fqcn);
        if (resourceClass == null) {
            modulesListOriginatingElements.add(typeElem);
            When when = typeElem.getAnnotation(When.class);
            group.resourceClasses.put(fqcn, resourceClass = new ResourceClass(group, fqcn, r.priority(),
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
                String getParamValueCode = parameter.kind.fetchFromReqCode(parameter, resourceMethod);
                if (!String.class.getName().equals(parameter.type)
                        && parameter.kind != ResourceMethodParameterKind.BODY
                        && parameter.kind != ResourceMethodParameterKind.CONTEXT) {
//                    getParamValueCode = String.format("converter.convert(%s, %s.class)", getParamValueCode, parameter.type);
                } else if (parameter.kind == ResourceMethodParameterKind.BODY) {
                    inEntityClass = parameter.type;
                }
                callParameters.add(String.format("/* [%s] %s */ %s", parameter.kind, parameter.name, getParamValueCode));

                if(parameter.kind.resolvedWithQueryParamMapper()) {
                    queryParametersDefinition.add(String.format("                    NamedType.of(%s, \"%s\")",
                            TypeHelper.getTypeReferenceExpressionFor(parameter.realType), parameter.name));
                }

                if (parameter.kind != ResourceMethodParameterKind.CONTEXT) {
                    parametersDescription.add(String.format(
                            "                OperationParameterDescription {PARAMETER} = new OperationParameterDescription();\n" +
                            "                {PARAMETER}.name = \"%s\";\n" +
                            "                {PARAMETER}.paramType = OperationParameterDescription.ParamType.%s;\n" +
                            "                {PARAMETER}.dataType = \"%s\";\n" +
                            "                {PARAMETER}.schemaKey = \"%s\";\n" +
                            "                {PARAMETER}.required = %s;\n" +
                            "                operation.parameters.add({PARAMETER});\n",
                            parameter.name,
                            parameter.kind.name().toLowerCase(),
                            toTypeDescription(parameter.type),
                            toSchemaKey(parameter.type),
                            String.valueOf(!parameter.guavaOptional && !parameter.java8Optional)
                    ).replaceAll("\\{PARAMETER}", parameter.name));
                }
            }

            String call = "resource." + resourceMethod.name + "(\n" +
                    "                        " +
                    Joiner.on(",\n                        ").join(callParameters) + "\n" +
                    "                )";

            if (resourceMethod.returnType.equalsIgnoreCase("void")) {
                call = call + ";\n" +
                        "                return Optional.of(Empty.EMPTY);";
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
                    .put("call", call)
                    .put("responseClass", toTypeDescription(resourceMethod.returnType))
                    .put("sourceLocation", resourceMethod.sourceLocation)
                    .put("parametersDescription", Joiner.on("\n").join(parametersDescription))
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

    private String toTypeDescription(String type) {
        // see https://github.com/wordnik/swagger-core/wiki/datatypes
        boolean isList = false;
        Pattern p = Pattern.compile("java\\.lang\\.Iterable<(.+)>");
        Matcher m = p.matcher(type);
        if (m.matches()) {
            type = m.group(1);
            isList = true;
        }
        boolean primitive = type.startsWith("java.lang");
        type =  type.substring(type.lastIndexOf('.') + 1);
        if ("Integer".equals(type)) {
            type = "int";
        }
        if (primitive) {
            type = type.toLowerCase();
        }
        if ("DateTime".equals(type) || "DateMidnight".equals(type)) {
            type = "Date";
        }

        if (isList) {
            return "LIST[" + type + "]";
        } else {
            return type;
        }
    }

    private Collection<ResourceMethodAnnotation> getResourceMethodAnnotationsInRound(RoundEnvironment roundEnv) {
        Collection<ResourceMethodAnnotation> methodAnnotations = Lists.newArrayList();
        for (Element resourceElem : roundEnv.getElementsAnnotatedWith(RestxResource.class)) {
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

    private static class ResourceMethod {
        final String httpMethod;
        final String path;
        final String name;
        final String realReturnType;
        final boolean returnTypeGuavaOptional;
        final boolean returnTypeJava8Optional;
        final String returnType;
        final String id;
        final ImmutableList<String> pathParamNames;
        final HttpStatus successStatus;
        final RestxLogLevel logLevel;
        final String permission;
        final String sourceLocation;
        final Optional<String> inContentType;
        final Optional<String> outContentType;

        final List<ResourceMethodParameter> parameters = Lists.newArrayList();

        ResourceMethod(ResourceClass resourceClass, String httpMethod, String path, String name, String returnType,
                       HttpStatus successStatus, RestxLogLevel logLevel, String permission, String sourceLocation,
                       Optional<String> inContentType, Optional<String> outContentType) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.name = name;
            this.logLevel = logLevel;
            this.permission = permission;
            this.sourceLocation = sourceLocation;
            this.inContentType = inContentType;
            this.outContentType = outContentType;

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
        }
    }

    private static class ResourceMethodParameter {
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
                // TODO: we should check the type, in case of list, use getQueryParams,
                // and we should better handle missing params
                String code = String.format("request.getQueryParam(\"%s\")", parameter.name);
                if (parameter.guavaOptional) {
                    code = code;
                } else if (parameter.java8Optional) {
                    code = String.format("java.util.Optional.ofNullable(%s.orNull())", code);
                } else {
                    code = String.format("checkPresent(%s, \"query param %s is required\")", code, parameter.name);
                }
                return code;
            }
        },
        PATH(true) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                return String.format("match.getPathParam(\"%s\")", parameter.name);
            }
        },
        BODY(false) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                return checkValidStr(parameter, "body");
            }
        },
        CONTEXT(false) {
            public String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method) {
                Collection<String> contextParamNames = Arrays.asList("baseUri", "clientAddress", "request", "locale", "locales");
                if (!contextParamNames.contains(parameter.reqParamName)) {
                    throw new IllegalArgumentException("context parameter not known: " + parameter.reqParamName +
                            ". Possible names are: " + Joiner.on(", ").join(contextParamNames));
                }
                switch (parameter.reqParamName) {
                    case "request":
                        return "request";
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

        private static String checkValidStr(ResourceMethodParameter parameter, String baseExpr) {
            Optional<String> validationGroupsExpr = parameter.joinedValidationGroupFQNExpression();
            return String.format("checkValid(validator, %s%s)", baseExpr, validationGroupsExpr.isPresent()?","+validationGroupsExpr.get():"");
        }

        public abstract String fetchFromReqCode(ResourceMethodParameter parameter, ResourceMethod method);

        public boolean resolvedWithQueryParamMapper() {
            return resolvedWithQueryParamMapper;
        }
    }

}
