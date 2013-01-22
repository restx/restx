package restx.annotations.processor;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import restx.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    final Tpl routerModuleTpl;
    final Tpl routerTpl;
    final Tpl routeTpl;

    public RestxAnnotationProcessor() {
        try {
            routerModuleTpl = new Tpl("RestxRouterModule");
            routerTpl = new Tpl("RestxRouter");
            routeTpl = new Tpl("RestxRoute");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

                ResourceGroup group = getResourceGroup(r, groups);
                ResourceClass resourceClass = getResourceClass(typeElem, r, group, modulesListOriginatingElements);

                ResourceMethod resourceMethod = new ResourceMethod(
                        resourceClass,
                        annotation.httpMethod, annotation.path,
                        annotation.methodElem.getSimpleName().toString());
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

            try {
                r.modules();
            } catch (MirroredTypesException e) {
                for (TypeMirror typeMirror : e.getTypeMirrors()) {
                    resourceClass.includeModules.add(String.format("                %s.class",
                            typeMirror.toString()));
                }
            }
        }
        return resourceClass;
    }

    private void generateFiles(Map<String, ResourceGroup> groups, Set<Element> modulesListOriginatingElements) throws IOException {
        FileObject resource = processingEnv.getFiler().createResource(
                StandardLocation.SOURCE_OUTPUT, "", "META-INF/services/restx.RestxRouterModule",
                Iterables.toArray(modulesListOriginatingElements, Element.class));
        Writer modules = resource.openWriter();
        for (ResourceGroup group : groups.values()) {
            for (ResourceClass resourceClass: group.resourceClasses.values()) {
                modules.write(resourceClass.fqcn + "RouterModule");

                List<String> routes = Lists.newArrayList();
                List<String> injectRoutes = Lists.newArrayList();
                List<String> provideRoutes = Lists.newArrayList();

                buildResourceRoutesCodeChunks(resourceClass, routes, injectRoutes, provideRoutes);

                ImmutableMap<String, String> ctx = ImmutableMap.<String, String>builder()
                        .put("package", resourceClass.pack)
                        .put("router", resourceClass.name + "Router")
                        .put("countRoutes", String.valueOf(routes.size()))
                        .put("includeModules", Joiner.on(",\n").join(resourceClass.includeModules))
                        .put("routes", Joiner.on(",\n").join(routes))
                        .put("provideRoutes", Joiner.on("\n\n").join(provideRoutes))
                        .put("injectRoutes", Joiner.on("\n").join(injectRoutes))
                        .build();

                generateJavaClass(resourceClass.fqcn + "Router", routerTpl.bind(ctx), resourceClass.originatingElements);
                generateJavaClass(resourceClass.fqcn + "RouterModule", routerModuleTpl.bind(ctx), resourceClass.originatingElements);
            }

        }
        modules.close();
    }

    private void generateJavaClass(String className, String code, Set<Element> originatingElements) throws IOException {
        JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(className,
                Iterables.toArray(originatingElements, Element.class));
        Writer writer = fileObject.openWriter();
        writer.write(code);
        writer.close();
    }

    private void buildResourceRoutesCodeChunks(ResourceClass resourceClass, List<String> routes, List<String> injectRoutes, List<String> provideRoutes) {
        for (ResourceMethod resourceMethod : resourceClass.resourceMethods) {
            routes.add("                " + resourceMethod.name);
            injectRoutes.add(String.format(
                    "    @Inject @Named(\"%s\") RestxRoute %s;", resourceMethod.id, resourceMethod.name));

            List<String> callParameters = Lists.newArrayList();

            for (ResourceMethodParameter parameter : resourceMethod.parameters) {
                String getParamValueCode = parameter.kind.fetchFromReqCode(parameter.name, parameter.type);
                if (!String.class.getName().equals(parameter.type)
                        && parameter.kind != ResourceMethodParameterKind.BODY) {
                    throw new UnsupportedOperationException("TODO handle type conversion");
                }
                callParameters.add(getParamValueCode);
            }

            provideRoutes.add(routeTpl.bind(ImmutableMap.<String, String>builder()
                    .put("routeId", resourceMethod.id)
                    .put("routeName", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, resourceMethod.name))
                    .put("method", resourceMethod.httpMethod)
                    .put("path", resourceMethod.path)
                    .put("resource", resourceClass.name)
                    .put("call", resourceMethod.name + "( " + Joiner.on(", ").join(callParameters) + " )")
                    .build()
            ));
        }
    }

    private Collection<ResourceMethodAnnotation> getResourceMethodAnnotationsInRound(RoundEnvironment roundEnv) {
        Collection<ResourceMethodAnnotation> methodAnnotations = Lists.newArrayList();
        // iterating through these annotations would be nicer, but we would need to use reflection for "value()"
        for (Element elem : roundEnv.getElementsAnnotatedWith(GET.class)) {
            GET annotation = elem.getAnnotation(GET.class);
            methodAnnotations.add(new ResourceMethodAnnotation("GET", elem, annotation.value()));
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(POST.class)) {
            POST annotation = elem.getAnnotation(POST.class);
            methodAnnotations.add(new ResourceMethodAnnotation("POST", elem, annotation.value()));
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(PUT.class)) {
            PUT annotation = elem.getAnnotation(PUT.class);
            methodAnnotations.add(new ResourceMethodAnnotation("PUT", elem, annotation.value()));
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(DELETE.class)) {
            DELETE annotation = elem.getAnnotation(DELETE.class);
            methodAnnotations.add(new ResourceMethodAnnotation("DELETE", elem, annotation.value()));
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(HEAD.class)) {
            HEAD annotation = elem.getAnnotation(HEAD.class);
            methodAnnotations.add(new ResourceMethodAnnotation("HEAD", elem, annotation.value()));
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
        final List<ResourceMethod> resourceMethods = Lists.newArrayList();
        final ResourceGroup group;
        final String name;
        final Set<Element> originatingElements = Sets.newHashSet();
        final List<String> includeModules = Lists.newArrayList("                RestxCoreModule.class");

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
        final String id;
        final Collection<String> pathParamNames;

        final List<ResourceMethodParameter> parameters = Lists.newArrayList();

        ResourceMethod(ResourceClass resourceClass, String httpMethod, String path, String name) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.name = name;
            this.id = resourceClass.group.name + "#" + resourceClass.name + "#" + name;
            Matcher matcher = pathParamNamesPattern.matcher(path);
            pathParamNames = Sets.newHashSet();
            while (matcher.find()) {
                pathParamNames.add(matcher.group(1));
            }
        }
    }

    private static class ResourceMethodParameter {
        final String type;
        final String name;
        final String reqParamName;
        final ResourceMethodParameterKind kind;

        private ResourceMethodParameter(String type, String name, String reqParamName, ResourceMethodParameterKind kind) {
            this.type = type;
            this.name = name;
            this.reqParamName = reqParamName;
            this.kind = kind;
        }
    }

    private static enum ResourceMethodParameterKind {
        QUERY {
            public String fetchFromReqCode(String name, String type) {
                throw new UnsupportedOperationException("TODO");
            }
        },
        PATH {
            public String fetchFromReqCode(String name, String type) {
                return String.format("match.getPathParams().get(\"%s\")", name);
            }
        },
        BODY {
            public String fetchFromReqCode(String name, String type) {
                return String.format("mapper.readValue(request.getContentStream(), %s.class)", type);
            }
        };

        public abstract String fetchFromReqCode(String name, String type);
    }
}
