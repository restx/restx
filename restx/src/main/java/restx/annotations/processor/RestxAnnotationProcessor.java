package restx.annotations.processor;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import restx.annotations.GET;
import restx.annotations.PathParam;
import restx.annotations.RestxResource;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 10:02 PM
 */
@SupportedAnnotationTypes({
        "restx.annotations.RestxResource",
        "restx.annotations.RestxBaseModule",
        "restx.annotations.GET"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RestxAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            final Map<String, ResourceGroup> groups = Maps.newHashMap();
            final Set<Element> modulesListOriginatingElements = Sets.newHashSet();

            for (Element elem : roundEnv.getElementsAnnotatedWith(GET.class)) {
                GET get = elem.getAnnotation(GET.class);
                TypeElement typeElem = (TypeElement) elem.getEnclosingElement();
                RestxResource r = typeElem.getAnnotation(RestxResource.class);

                String fqcn = typeElem.getQualifiedName().toString();

                ResourceGroup group = groups.get(r.group());
                if (group == null) {
                    groups.put(r.group(), group = new ResourceGroup(r.group()));
                }

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

                ResourceMethod resourceMethod = new ResourceMethod(
                        resourceClass, "GET", get.value(), elem.getSimpleName().toString());
                resourceClass.resourceMethods.add(resourceMethod);
                resourceClass.originatingElements.add(elem);

                for (VariableElement p : ((ExecutableElement) elem).getParameters()) {
                    PathParam pathParam = p.getAnnotation(PathParam.class); // TODO handle other kind of params

                    resourceMethod.parameters.add(new ResourceMethodParameter(
                            "java.lang.String", // TODO
                            p.getSimpleName().toString(),
                            pathParam.value().length() == 0 ? p.getSimpleName().toString() : pathParam.value(),
                            ResourceMethodParameterKind.PATH));
                }
            }

            if (!groups.isEmpty()) {
                generateFiles(groups, modulesListOriginatingElements);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void generateFiles(Map<String, ResourceGroup> groups, Set<Element> modulesListOriginatingElements) throws IOException {
        final Tpl routerModuleTpl = new Tpl("RestxRouterModule");
        final Tpl routerTpl = new Tpl("RestxRouter");
        final Tpl routeTpl = new Tpl("RestxRoute");

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

                for (ResourceMethod resourceMethod : resourceClass.resourceMethods) {
                    routes.add("                " + resourceMethod.name);
                    injectRoutes.add(String.format(
                            "    @Inject @Named(\"%s\") RestxRoute %s;", resourceMethod.id, resourceMethod.name));

                    List<String> callParameters = Lists.newArrayList();

                    for (ResourceMethodParameter parameter : resourceMethod.parameters) {
                        String getParamValueCode = parameter.kind.fetchFromReqCode(parameter.name);
                        if (!String.class.getName().equals(parameter.type)) {
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

                ImmutableMap<String, String> ctx = ImmutableMap.<String, String>builder()
                        .put("package", resourceClass.pack)
                        .put("router", resourceClass.name + "Router")
                        .put("countRoutes", String.valueOf(routes.size()))
                        .put("includeModules", Joiner.on(",\n").join(resourceClass.includeModules))
                        .put("routes", Joiner.on(",\n").join(routes))
                        .put("provideRoutes", Joiner.on("\n\n").join(provideRoutes))
                        .put("injectRoutes", Joiner.on("\n").join(injectRoutes))
                        .build();

                JavaFileObject router = processingEnv.getFiler().createSourceFile(resourceClass.fqcn + "Router",
                        Iterables.toArray(resourceClass.originatingElements, Element.class));
                Writer writer = router.openWriter();
                writer.write(routerTpl.bind(ctx));
                writer.close();
                JavaFileObject routerModule = processingEnv.getFiler().createSourceFile(resourceClass.fqcn + "RouterModule",
                        Iterables.toArray(resourceClass.originatingElements, Element.class));
                writer = routerModule.openWriter();
                writer.write(routerModuleTpl.bind(ctx));
                writer.close();
            }

        }

        modules.close();
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
        final String httpMethod;
        final String path;
        final String name;
        final String id;

        final List<ResourceMethodParameter> parameters = Lists.newArrayList();

        ResourceMethod(ResourceClass resourceClass, String httpMethod, String path, String name) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.name = name;
            this.id = resourceClass.group.name + "#" + resourceClass.name + "#" + name;
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
            public String fetchFromReqCode(String name) {
                throw new UnsupportedOperationException("TODO");
            }
        },
        PATH {
            public String fetchFromReqCode(String name) {
                return String.format("match.getPathParams().get(\"%s\")", name);
            }
        },
        BODY {
            public String fetchFromReqCode(String name) {
                throw new UnsupportedOperationException("TODO");
            }
        };

        public abstract String fetchFromReqCode(String name);
    }
}
