package restx.server.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.*;
import restx.server.simple.simple.SimpleWebServer;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 4:24 PM
 */
public class WebServerExample {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final RestxMainRouter ROUTER = StdRestxMainRouter.builder()
            .withMapper(mapper)
            .addRoute("GET", "/route1/{id}", new MatchedEntityRoute() {
                @Override
                public Optional<?> route(RestxRequest restxRequest, RestxRequestMatch match) throws IOException {
                    return Optional.of(ImmutableMap.of("id", match.getPathParams().get("id")));
                }
            })
            .addRoute("GET", "/route2", new MatchedEntityRoute() {
                @Override
                public Optional<?> route(RestxRequest restxRequest, RestxRequestMatch match) throws IOException {
                    return Optional.of(ImmutableMap.of("path", "route2"));
                }
            })
            .addRoute("GET", "/route3", new MatchedEntityRoute() {
                @Override
                public Optional<?> route(RestxRequest restxRequest, RestxRequestMatch match) throws IOException {
                    return Optional.of(ImmutableMap.of("path", "route3"));
                }
            })
            .build();


    public static void main(String[] args) throws Exception {
        SimpleWebServer.builder().setRouter(ROUTER).setRouterPath("/api").setPort(8080).build().startAndAwait();
    }
}
