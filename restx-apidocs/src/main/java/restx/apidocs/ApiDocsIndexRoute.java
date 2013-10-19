package restx.apidocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.*;
import restx.factory.Component;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.jackson.FrontObjectMapperFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
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
public class ApiDocsIndexRoute extends StdEntityRoute {
    private final Factory factory;

    @Inject
    public ApiDocsIndexRoute(@Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper, Factory factory) {
        super("ApiDocsIndexRoute", mapper, new StdRestxRequestMatcher("GET", "/@/api-docs"));
        this.factory = factory;
    }

    @Override
    protected Optional<?> doRoute(RestxRequest restxRequest, RestxRequestMatch match) throws IOException {
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
            apis.add(ImmutableMap.of(
                    "path", "/@/api-docs/" + routerApiPath,
                    "name", routerApiPath,
                    "group", router.getComponent().getGroupName(),
                    "description", ""));
        }
        return apis;
    }

    private String getRouterApiPath(String path) {
        path = path.replaceAll("Router$", "").replaceAll("Resource$", "");
        path = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, path);
        return path;
    }
}
