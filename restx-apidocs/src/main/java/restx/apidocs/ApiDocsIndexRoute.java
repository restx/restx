package restx.apidocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.*;
import restx.factory.Component;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.jackson.FrontObjectMapperFactory;
import restx.jackson.StdJsonProducerEntityRoute;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serves the swagger resource listing, which looks like that:
 * {
 *    "apiVersion":"0.2",
 *    "swaggerVersion":"1.1",
 *    "basePath":"http://petstore.swagger.wordnik.com/api",
 *    "apis":[
 *      {
 *          "path":"/api-docs.{format}/user",
 *          "description":""
 *      },
 *      {
 *          "path":"/api-docs.{format}/pet",
 *          "description":""
 *      }
 *    ]
 * }
 */
@Component
public class ApiDocsIndexRoute extends StdJsonProducerEntityRoute {
    private final Factory factory;

    @Inject
    public ApiDocsIndexRoute(@Named(FrontObjectMapperFactory.WRITER_NAME) ObjectWriter writer, Factory factory) {
        super("ApiDocsIndexRoute", Map.class, writer, new StdRestxRequestMatcher("GET", "/@/api-docs"));
        this.factory = factory;
    }

    @Override
    protected Optional<?> doRoute(RestxRequest restxRequest, RestxRequestMatch match, Object i) throws IOException {
        return Optional.of(ImmutableMap.builder()
                .put("apiVersion", "0.1") // TODO
                .put("swaggerVersion", "1.1")
                .put("basePath", restxRequest.getBaseNetworkPath())
                .put("apis", buildApis())
                .build());
    }

    private List<ImmutableMap<String, String>> buildApis() {
        Set<NamedComponent<RestxRouter>> routers = factory.queryByClass(RestxRouter.class).find();
        List<ImmutableMap<String, String>> apis = Lists.newArrayList();
        for (NamedComponent<RestxRouter> router : routers) {
            String routerApiPath = getRouterApiPath(router.getName().getName());
            if (ApiDeclarationRoute.getRouterByName(factory, routerApiPath).isPresent()) {
                // we add the api only if we can find back the router from the name, otherwise it will trigger
                // 404 errors in API-DOCS
                apis.add(ImmutableMap.of(
                        "path", "/@/api-docs/" + routerApiPath,
                        "name", routerApiPath,
                        "group", router.getComponent().getGroupName(),
                        "description", ""));
            }
        }
        return apis;
    }

    static String getRouterApiPath(String path) {
        path = path.replaceAll("Router$", "").replaceAll("Resource$", "");
        path = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, path);
        return path;
    }
}
