package restx.server.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import restx.*;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 4:24 PM
 */
public class SimpleWebServerExample {

    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        new SimpleWebServer(new StdRestxMainRouter(ImmutableList.of(
                route1(),
                route2(),
                route3()
        )), "/api", "", 8080).startAndAwait();
    }

    private static RestxRoute route3() {
        return new StdRoute("route3", mapper, new StdRouteMatcher("GET", "/route3")) {
            @Override
            protected Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException {
                return Optional.of(ImmutableMap.of("name", "route3"));
            }
        };
    }

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


}
