package restx.server.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import restx.*;
import restx.entity.MatchedEntityOutputRoute;
import restx.entity.MatchedEntityRoute;
import restx.server.WebServer;
import restx.server.WebServers;
import restx.server.simple.simple.SimpleWebServer;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 27/11/13
 * Time: 21:24
 */
public class SimpleWebServerTest {
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

    @Test
    public void should_handle_simple_routes() throws Exception {
        SimpleWebServer server = SimpleWebServer.builder().setRouter(ROUTER)
                .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
        server.start();
        try {
            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/route1/xavier");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("{\"id\":\"xavier\"}");

            httpRequest = HttpRequest.put(server.baseUrl() + "/api/route4").send("{\"test\":\"val1\"}");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("{\"test\":\"val1\",\"size\":1}");
        } finally {
            server.stop();
        }
    }
}
