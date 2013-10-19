package restx.apidocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.*;
import restx.description.DescribableRoute;
import restx.description.ResourceDescription;
import restx.factory.Component;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.NamedComponent;
import restx.jackson.FrontObjectMapperFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

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
public class ApiDeclarationRoute extends StdEntityRoute {
    private final Factory factory;

    @Inject
    public ApiDeclarationRoute(@Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper,
                               Factory factory) {
        super("ApiDeclarationRoute", mapper, new StdRestxRequestMatcher("GET", "/@/api-docs/{router}"));
        this.factory = factory;
    }

    @Override
    protected Optional<?> doRoute(RestxRequest restxRequest, RestxRequestMatch match) throws IOException {
        String routerName = match.getPathParam("router");
        routerName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, routerName);

        Optional<NamedComponent<RestxRouter>> router = Optional.absent();
        ImmutableList<String> suffixes = ImmutableList.of("ResourceRouter", "", "Resource", "Router");
        for (int i = 0; i < suffixes.size() && !router.isPresent(); i++) {
            router = factory.queryByName(Name.of(RestxRouter.class, routerName + suffixes.get(i))).optional().findOne();
        }

        if (!router.isPresent()) {
            return Optional.absent();
        }

        List<ResourceDescription> apis = buildApis(router.get());
        return Optional.of(ImmutableMap.builder()
                .put("apiVersion", "0.1") // TODO
                .put("swaggerVersion", "1.1")
                .put("basePath", restxRequest.getBaseNetworkPath())
                .put("apis", apis)
                .build());
    }

    private List<ResourceDescription> buildApis(NamedComponent<RestxRouter> router) {
        ImmutableList<RestxRoute> routes = router.getComponent().getRoutes();
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
