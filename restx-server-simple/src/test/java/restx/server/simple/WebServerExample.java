package restx.server.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.*;
import restx.entity.MatchedEntityOutputRoute;
import restx.entity.MatchedEntityRoute;
import restx.server.simple.simple.SimpleWebServer;

import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 4:24 PM
 */
public class WebServerExample {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final RestxMainRouter ROUTER = StdRestxMainRouter.builder()
            .addRouter(RestxRouter.builder()
                    .withMapper(mapper)
                    .GET("/route1/{id}", new MatchedEntityOutputRoute() {
                        @Override
                        public Optional route(RestxRequest restxRequest, RestxRequestMatch match) {
                            return Optional.of(ImmutableMap.of("id", match.getPathParam("id")));
                        }
                    })
                    .GET("/route2", new MatchedEntityOutputRoute() {
                        @Override
                        public Optional<?> route(RestxRequest restxRequest, RestxRequestMatch match) throws IOException {
                            return Optional.of(ImmutableMap.of("path", "route2"));
                        }
                    })
                    .GET("/route3", new MatchedEntityOutputRoute() {
                        @Override
                        public Optional<?> route(RestxRequest restxRequest, RestxRequestMatch match) throws IOException {
                            return Optional.of(ImmutableMap.of("path", "route3"));
                        }
                    })
                    .PUT("/route4", Map.class, new MatchedEntityRoute<Map, Map>() {
                        @Override
                        public Optional<Map> route(RestxRequest restxRequest, RestxRequestMatch match, Map input) throws IOException {
                            input.put("size", input.size());
                            return Optional.of(input);
                        }
                    })
                    .build())
            .build();


    public static void main(String[] args) throws Exception {
        SimpleWebServer.builder().setRouter(ROUTER).setRouterPath("/api").setPort(8080).build().startAndAwait();
    }
}
