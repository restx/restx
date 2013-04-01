package restx.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.*;
import restx.factory.Component;
import restx.factory.Factory;
import restx.factory.Name;
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
public class SwaggerIndexRoute extends StdEntityRoute {
    private final Factory factory;

    @Inject
    public SwaggerIndexRoute(@Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper, Factory factory) {
        super("SwaggerIndexRoute", mapper, new StdRouteMatcher("GET", "/@/api-docs"));
        this.factory = factory;
    }

    @Override
    protected Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException {
        return Optional.of(ImmutableMap.builder()
                .put("apiVersion", "0.1") // TODO
                .put("swaggerVersion", "1.1")
                .put("basePath", restxRequest.getBaseUri())
                .put("apis", buildApis())
                .build());
    }

    private List<ImmutableMap<String, String>> buildApis() {
        Set<NamedComponent<RestxRoute>> routes = factory.queryByClass(RestxRoute.class).find();
        List<ImmutableMap<String, String>> apis = Lists.newArrayList();
        for (NamedComponent<RestxRoute> route : routes) {
            if (route.getComponent() instanceof RestxRouter) {
                apis.add(ImmutableMap.of("path", "/@/api-docs/" + getRouterApiPath(route.getName()),
                        "description", ""));
            }
        }
        return apis;
    }

    private String getRouterApiPath(Name<?> name) {
        String path = name.getName();
        if (path.endsWith("Router")) {
            path = path.substring(0, path.length() - "Router".length());
        }
        if (path.endsWith("Resource")) {
            path = path.substring(0, path.length() - "Resource".length());
        }
        path = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, path);
        return path;
    }
}
