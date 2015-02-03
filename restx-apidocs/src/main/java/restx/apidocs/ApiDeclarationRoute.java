package restx.apidocs;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import restx.*;
import restx.description.*;
import restx.factory.Component;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.NamedComponent;
import restx.jackson.FrontObjectMapperFactory;
import restx.jackson.StdJsonProducerEntityRoute;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.*;

import static restx.apidocs.ApiDocsIndexRoute.getRouterApiPath;

/**
 * Serves the swagger api declaration of one router, which looks like that:
 * <pre>
 {
   apiVersion: "0.2",
   swaggerVersion: "1.1",
   basePath: "http://petstore.swagger.wordnik.com/api",
   resourcePath: "/pet.{format}"

   ...

   apis: [...]
   models: {...}
 }
 </pre>
 *
 * See <a href="https://github.com/wordnik/swagger-core/wiki/API-Declaration">API Declaration</a>
 */
@Component
public class ApiDeclarationRoute extends StdJsonProducerEntityRoute {
    private final Factory factory;

    @Inject
    public ApiDeclarationRoute(@Named(FrontObjectMapperFactory.WRITER_NAME) ObjectWriter writer,
                               Factory factory) {
        super("ApiDeclarationRoute", Map.class, writer, new StdRestxRequestMatcher("GET", "/@/api-docs/{router}"));
        this.factory = factory;
    }

    @Override
    protected Optional<?> doRoute(RestxRequest restxRequest, RestxRequestMatch match, Object body) throws IOException {
        String routerName = match.getPathParam("router");
        Optional<NamedComponent<RestxRouter>> router = getRouterByName(factory, routerName);

        if (!router.isPresent()) {
            return Optional.absent();
        }

        List<ResourceDescription> apis = buildApis(router.get());
        return Optional.of(ImmutableMap.builder()
                .put("apiVersion", "0.1") // TODO
                .put("swaggerVersion", "1.1")
                .put("basePath", restxRequest.getBaseNetworkPath())
                .put("name", router.get().getComponent().getClass().getName().replaceAll("Router$", ""))
                .put("apis", apis)
                .build());
    }

    static Optional<NamedComponent<RestxRouter>> getRouterByName(Factory f, String routerName) {
        routerName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, routerName);

        Optional<NamedComponent<RestxRouter>> router = Optional.absent();
        ImmutableList<String> suffixes = ImmutableList.of("ResourceRouter", "", "Resource", "Router");
        for (int i = 0; i < suffixes.size() && !router.isPresent(); i++) {
            router = f.queryByName(Name.of(RestxRouter.class, routerName + suffixes.get(i))).optional().findOne();
        }
        return router;
    }

    private List<ResourceDescription> buildApis(NamedComponent<RestxRouter> router) {
        return fillRelatedOperations(router.getName().getName(), describeAllRoutes(router.getComponent()));
    }

    private List<ResourceDescription> fillRelatedOperations(final String name, List<ResourceDescription> apis) {
        Multimap<String, OperationReference> operationsByType = getOperationReferencesByType();
        Multimap<String, OperationReference> operationsByPath = getOperationReferencesByPath();
        String routerApiPath = getRouterApiPath(name);
        for (final ResourceDescription api : apis) {
            for (final OperationDescription operation : api.operations) {
                Set<OperationReference> related = new LinkedHashSet<>(operation.relatedOperations);

                // add related by type
                related.addAll(operationsByType.get(getTargetType(operation.responseClass)));
                Optional<OperationParameterDescription> bodyParameter = operation.findBodyParameter();
                if (bodyParameter.isPresent()) {
                    related.addAll(operationsByType.get(getTargetType(bodyParameter.get().dataType)));
                }

                // add related by path
                for (OperationReference operationReference : operationsByPath.get(api.path)) {
                    related.add(operationReference);
                }

                // remove reference to self
                for (Iterator<OperationReference> iterator = related.iterator(); iterator.hasNext(); ) {
                    OperationReference operationReference = iterator.next();
                    if ( routerApiPath.equals(operationReference.apiDocName)
                            && api.path.equals(operationReference.path)
                            && operation.httpMethod.equals(operationReference.httpMethod)) {
                        iterator.remove();
                        break;
                    }
                }

                operation.relatedOperations = new ArrayList<>(related);
            }
        }

        return apis;
    }

    private Multimap<String, OperationReference> getOperationReferencesByType() {
        Multimap<String, OperationReference> operationsByType = ArrayListMultimap.create();
        Set<NamedComponent<RestxRouter>> routers = factory.queryByClass(RestxRouter.class).find();
        for (NamedComponent<RestxRouter> r : routers) {
            String routerApiPath = getRouterApiPath(r.getName().getName());

            for (ResourceDescription resourceDescription : describeAllRoutes(r.getComponent())) {
                for (OperationDescription operation : resourceDescription.operations) {
                    OperationReference ref = buildReferenceFor(routerApiPath, resourceDescription.path, operation);
                    addIfRelevant(operationsByType, ref, operation.responseClass);
                    Optional<OperationParameterDescription> bodyParameter = operation.findBodyParameter();
                    if (bodyParameter.isPresent()) {
                        addIfRelevant(operationsByType, ref, bodyParameter.get().dataType);
                    }
                }
            }
        }
        return operationsByType;
    }

    private Multimap<String, OperationReference> getOperationReferencesByPath() {
        Multimap<String, OperationReference> operationsByPath = ArrayListMultimap.create();
        Set<NamedComponent<RestxRouter>> routers = factory.queryByClass(RestxRouter.class).find();
        for (NamedComponent<RestxRouter> r : routers) {
            String routerApiPath = getRouterApiPath(r.getName().getName());

            for (ResourceDescription resourceDescription : describeAllRoutes(r.getComponent())) {
                for (OperationDescription operation : resourceDescription.operations) {
                    OperationReference ref = buildReferenceFor(routerApiPath, resourceDescription.path, operation);
                    operationsByPath.put(resourceDescription.path, ref);
                }
            }
        }
        return operationsByPath;
    }

    private void addIfRelevant(Multimap<String, OperationReference> operationsByType, OperationReference ref, String dataType) {
        String targetType = getTargetType(dataType);
        if (!targetType.equals("void") && !targetType.equals("Status")) {
            operationsByType.put(targetType, ref);
        }
    }

    private String getTargetType(String dataType) {
        return dataType == null ? "void" : dataType.replaceAll("LIST\\[(.+)\\]", "$1");
    }

    private OperationReference buildReferenceFor(String routerApiPath, String path, OperationDescription operation) {
        OperationReference ref = new OperationReference();
        ref.apiDocName = routerApiPath;
        ref.path = path;
        ref.httpMethod = operation.httpMethod;
        ref.responseClass = operation.responseClass;
        Optional<OperationParameterDescription> bodyParameter = operation.findBodyParameter();
        ref.requestClass = bodyParameter.isPresent() ? bodyParameter.get().dataType : "void";
        return ref;
    }

    private List<ResourceDescription> describeAllRoutes(RestxRouter component) {
        ImmutableList<RestxRoute> routes = component.getRoutes();
        List<ResourceDescription> apis = Lists.newArrayList();
        for (RestxRoute route : routes) {
            if (route instanceof DescribableRoute) {
                DescribableRoute describableRoute = (DescribableRoute) route;
                apis.addAll(describableRoute.describe());
            }
        }
        return apis;
    }

}
