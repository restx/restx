package restx.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import restx.*;
import restx.server.simple.SimpleWebServer;
import restx.servlet.AbstractRestxMainRouterServlet;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 4:24 PM
 */
public class WebServerExample {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final StdRestxMainRouter ROUTER = new StdRestxMainRouter(ImmutableList.of(
            route1(),
            route2(),
            route3()
    ));

    private static RestxRoute route1() {
        return new StdRoute("route1", mapper, new StdRouteMatcher("GET", "/route1/{id}")) {
            @Override
            protected Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException {
                return Optional.of(ImmutableMap.of("id", match.getPathParams().get("id")));
            }
        };
    }

    private static RestxRoute route2() {
        return new StdRoute("route2", mapper, new StdRouteMatcher("GET", "/route2")) {
            @Override
            protected Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException {
                return Optional.of(ImmutableMap.of("name", "route2"));
            }
        };
    }

    private static RestxRoute route3() {
        return new StdRoute("route3", mapper, new StdRouteMatcher("GET", "/route3")) {
            @Override
            protected Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException {
                return Optional.of(ImmutableMap.of("name", "route3"));
            }
        };
    }

    public static void main(String[] args) throws Exception {
        String server = args.length > 0 ? args[0] : "jetty";

        WebServer webServer;
        switch (server) {
            case "simple":
                webServer = new SimpleWebServer(ROUTER, "/api", "", 8080);
                break;
            case "jetty":
                webServer = new JettyWebServer(
                        "restx/src/test/resources/restx/server/WebServerExample-web.xml",
                        ".", 8080, "localhost");
                break;
            default:
                throw new IllegalArgumentException("unknown server " + server);
        }
        webServer.startAndAwait();
    }

    public static class Servlet extends AbstractRestxMainRouterServlet {
        public Servlet() {
            super(ROUTER);
        }
    }

}
