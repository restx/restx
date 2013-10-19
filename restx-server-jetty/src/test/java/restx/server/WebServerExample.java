package restx.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.*;
import restx.servlet.AbstractRestxMainRouterServlet;

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
      WebServer webServer = new JettyWebServer(
              "restx/src/test/resources/restx/server/WebServerExample-web.xml",
              ".", 8080, "localhost");
      webServer.startAndAwait();
    }

    public static class Servlet extends AbstractRestxMainRouterServlet {
        public Servlet() {
            super(ROUTER);
        }
    }

}
