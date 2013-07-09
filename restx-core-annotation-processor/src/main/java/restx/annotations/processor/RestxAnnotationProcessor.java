package restx.annotations.processor;

import com.github.mustachejava.Mustache;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import restx.HttpStatus;
import restx.annotations.*;
import restx.common.Mustaches;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 10:02 PM
 */
@SupportedAnnotationTypes({
        "restx.annotations.RestxResource",
        "restx.annotations.GET",
        "restx.annotations.HEAD",
        "restx.annotations.POST",
        "restx.annotations.PUT",
        "restx.annotations.DELETE"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RestxAnnotationProcessor extends AbstractProcessor {
    final Mustache routerTpl;

    public RestxAnnotationProcessor() {
        routerTpl = Mustaches.compile(RestxAnnotationProcessor.class, "RestxRouter.mustache");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            final Map<String, ResourceGroup> groups = Maps.newHashMap();
            final Set<Element> modulesListOriginatingElements = Sets.newHashSet();

            for (ResourceMethodAnnotation annotation : getResourceMethodAnnotationsInRound(roundEnv)) {
                TypeElement typeElem = (TypeElement) annotation.methodElem.getEnclosingElement();
                RestxResource r = typeElem.getAnnotation(RestxResource.class);
                if (r == null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        String.format("%s rest method class must be annotated with RestxResourceparam",
                                annotation.methodElem.getSimpleName()), typeElem);
                }

                SuccessStatus successStatusAnn = annotation.methodElem.getAnnotation(SuccessStatus.class);
                HttpStatus successStatus = successStatusAnn==null?HttpStatus.OK:successStatusAnn.value();

                ResourceGroup group = getResourceGroup(r, groups);
                ResourceClass resourceClass = getResourceClass(typeElem, r, group, modulesListOriginatingElements);

                ResourceMethod resourceMethod = new ResourceMethod(
                        resourceClass,
                        annotation.httpMethod, annotation.path,
                        annotation.methodElem.getSimpleName().toString(),
                        annotation.methodElem.getReturnType().toString(),
                        successStatus);
                resourceClass.resourceMethods.add(resourceMethod);
                resourceClass.originatingElements.add(annotation.methodElem);

                buildResourceMethodParams(annotation, resourceMethod);
            }

            if (!groups.isEmpty()) {
                generateFiles(groups, modulesListOriginatingElements);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
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
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
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

            resourceMethod.parameters.add(new ResourceMethodParameter(
                p.asType().toString(),
                paramName,
                reqParamName,
                parameterKind));
        }
        if (!pathParamNamesToMatch.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
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
            group.resourceClasses.put(fqcn, resourceClass = new ResourceClass(group, fqcn));
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
                        .put("router", resourceClass.name + "Router")
                        .put("resource", resourceClass.name)
                        .put("routes", routes)
                        .build();

                generateJavaClass(resourceClass.fqcn + "Router", routerTpl, ctx, resourceClass.originatingElements);
            }

        }
    }

    private void generateJavaClass(String className, Mustache mustache, ImmutableMap<String, ? extends Object> ctx,
            Set<Element> originatingElements) throws IOException {
        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(className,
                Iterables.toArray(originatingElements, Element.class));
        try (Writer writer = fileObject.openWriter()) {
            mustache.execute(writer, ctx);
        }
    }

    private void buildResourceRoutesCodeChunks(ResourceClass resourceClass, List<ImmutableMap<String, Object>> routes) {
        for (ResourceMethod resourceMethod : resourceClass.resourceMethods) {

            List<String> callParameters = Lists.newArrayList();
            List<String> parametersDescription = Lists.newArrayList();

            for (ResourceMethodParameter parameter : resourceMethod.parameters) {
                String getParamValueCode = parameter.kind.fetchFromReqCode(parameter);
                if (!String.class.getName().equals(parameter.type)
                        && parameter.kind != ResourceMethodParameterKind.BODY) {
                    getParamValueCode = String.format("converter.convert(%s, %s.class)", getParamValueCode, parameter.type);
                }
                callParameters.add(String.format("/* [%s] %s */ %s", parameter.kind, parameter.name, getParamValueCode));

                if (parameter.kind != ResourceMethodParameterKind.CONTEXT) {
                    parametersDescription.add(String.format(
                            "                OperationParameterDescription {PARAMETER} = new OperationParameterDescription();\n" +
                            "                {PARAMETER}.name = \"%s\";\n" +
                            "                {PARAMETER}.paramType = OperationParameterDescription.ParamType.%s;\n" +
                            "                {PARAMETER}.dataType = \"%s\";\n" +
                            "                {PARAMETER}.required = %s;\n" +
                            "                operation.parameters.add({PARAMETER});\n",
                            parameter.name,
                            parameter.kind.name().toLowerCase(),
                            toTypeDescription(parameter.type),
                            String.valueOf(!parameter.optional)
                    ).replaceAll("\\{PARAMETER}", parameter.name));
                }
            }

            String call = "resource." + resourceMethod.name + "(\n" +
                    "                        " +
                    Joiner.on(",\n                        ").join(callParameters) + "\n" +
                    "                )";
            if (!resourceMethod.returnTypeOptional) {
                call = "Optional.of(" + call + ")";
            }

            routes.add(ImmutableMap.<String, Object>builder()
                    .put("routeId", resourceMethod.id)
                    .put("routeName", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, resourceMethod.name))
                    .put("method", resourceMethod.httpMethod)
                    .put("path", resourceMethod.path)
                    .put("resource", resourceClass.name)
                    .put("call", call)
                    .put("responseClass", toTypeDescription(resourceMethod.returnType))
                    .put("parametersDescription", Joiner.on("\n").join(parametersDescription))
                    .put("overrideWriteValue", resourceMethod.returnType.startsWith(Iterable.class.getName()) ?
                            String.format("protected ObjectWriter getObjectWriter(ObjectMapper mapper) { return super.getObjectWriter(mapper).withType(new TypeReference<%s>() { }); }", resourceMethod.returnType)
                            : "")
                    .put("successStatusName", resourceMethod.successStatus.name())
                    .build()
            );
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
        // iterating through these annotations would be nicer, but we would need to use reflection for "value()"
        for (Element elem : roundEnv.getElementsAnnotatedWith(GET.class)) {
            GET annotation = elem.getAnnotation(GET.class);
            if (annotation != null) {
                methodAnnotations.add(new ResourceMethodAnnotation("GET", elem, annotation.value()));
            }
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(POST.class)) {
            POST annotation = elem.getAnnotation(POST.class);
            if (annotation != null) {
                methodAnnotations.add(new ResourceMethodAnnotation("POST", elem, annotation.value()));
            }
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(PUT.class)) {
            PUT annotation = elem.getAnnotation(PUT.class);
            if (annotation != null) {
                methodAnnotations.add(new ResourceMethodAnnotation("PUT", elem, annotation.value()));
            }
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(DELETE.class)) {
            DELETE annotation = elem.getAnnotation(DELETE.class);
            if (annotation != null) {
                methodAnnotations.add(new ResourceMethodAnnotation("DELETE", elem, annotation.value()));
            }
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(HEAD.class)) {
            HEAD annotation = elem.getAnnotation(HEAD.class);
            if (annotation != null) {
                methodAnnotations.add(new ResourceMethodAnnotation("HEAD", elem, annotation.value()));
            }
        }
        return methodAnnotations;
    }

    static Pattern optionalPattern = Pattern.compile("\\Q" + Optional.class.getName() + "<\\E(.+)>");

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
        final List<ResourceMethod> resourceMethods = Lists.newArrayList();
        final ResourceGroup group;
        final String name;
        final Set<Element> originatingElements = Sets.newHashSet();

        ResourceClass(ResourceGroup group, String fqcn) {
            this.group = group;
            this.fqcn = fqcn;
            this.pack = fqcn.substring(0, fqcn.lastIndexOf('.'));
            this.name = fqcn.substring(fqcn.lastIndexOf('.') + 1);
        }
    }

    private static class ResourceMethod {
        private static final Pattern pathParamNamesPattern = Pattern.compile("\\{([a-zA-Z]+)}");
        final String httpMethod;
        final String path;
        final String name;
        final String realReturnType;
        final boolean returnTypeOptional;
        final String returnType;
        final String id;
        final Collection<String> pathParamNames;
        final HttpStatus successStatus;

        final List<ResourceMethodParameter> parameters = Lists.newArrayList();

        ResourceMethod(ResourceClass resourceClass, String httpMethod, String path, String name, String returnType, HttpStatus successStatus) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.name = name;
            Matcher m = optionalPattern.matcher(returnType);
            this.realReturnType = returnType;
            this.returnTypeOptional = m.matches();
            this.returnType = returnTypeOptional ? m.group(1) : returnType;
            this.id = resourceClass.group.name + "#" + resourceClass.name + "#" + name;
            this.successStatus = successStatus;
            Matcher matcher = pathParamNamesPattern.matcher(path);
            pathParamNames = Sets.newHashSet();
            while (matcher.find()) {
                pathParamNames.add(matcher.group(1));
            }
        }
    }

    private static class ResourceMethodParameter {
        final String type;
        final String realType;
        final boolean optional;
        final String name;
        final String reqParamName;
        final ResourceMethodParameterKind kind;

        private ResourceMethodParameter(String type, String name, String reqParamName, ResourceMethodParameterKind kind) {
            Matcher m = optionalPattern.matcher(type);
            this.realType = type;
            this.optional = m.matches();
            this.type = optional ? m.group(1) : type;
            this.name = name;
            this.reqParamName = reqParamName;
            this.kind = kind;
        }
    }

    private static enum ResourceMethodParameterKind {
        QUERY {
            public String fetchFromReqCode(ResourceMethodParameter parameter) {
                // TODO: we should check the type, in case of list, use getQueryParams,
                // and we should better handle missing params
                String code = String.format("request.getQueryParam(\"%s\")", parameter.name);
                if (!parameter.optional) {
                    code = String.format("checkPresent(%s, \"query param %s is required\")", code, parameter.name);
                }
                return code;
            }
        },
        PATH {
            public String fetchFromReqCode(ResourceMethodParameter parameter) {
                String code = String.format("match.getPathParams().get(\"%s\")", parameter.name);
                if (parameter.optional) {
                    code = "Optional.fromNullable(" + code + ")";
                } else {
                    code = String.format("checkNotNull(%s, \"path param %s is required\")", code, parameter.name);
                }
                return code;
            }
        },
        BODY {
            public String fetchFromReqCode(ResourceMethodParameter parameter) {
                return String.format("checkValid(validator, mapper.readValue(request.getContentStream(), %s.class))", parameter.type);
            }
        },
        CONTEXT {
            public String fetchFromReqCode(ResourceMethodParameter parameter) {
                Collection<String> contextParamNames = Arrays.asList("baseUri");
                if (!contextParamNames.contains(parameter.reqParamName)) {
                    throw new IllegalArgumentException("context parameter not known: " + parameter.reqParamName +
                            ". Possible names are: " + Joiner.on(", ").join(contextParamNames));
                }
                switch (parameter.reqParamName) {
                    case "baseUri":
                        return String.format("request.getBaseUri()", parameter.type);
                    default:
                        throw new IllegalStateException(
                                "invalid context param name not catched by contextParamNames list !! " + parameter.reqParamName);
                }
            }
        };

        public abstract String fetchFromReqCode(ResourceMethodParameter parameter);
    }
}
