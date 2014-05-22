package samplest.core;

import com.google.common.base.Optional;
import restx.RestxContext;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.RestxRouteFilter;
import restx.StdRestxRequestMatch;
import restx.StdRestxRequestMatcher;
import restx.StdRoute;
import restx.factory.Module;
import restx.factory.Provides;

import java.io.IOException;

/**
 * Date: 22/5/14
 * Time: 19:30
 */
@Module
public class RouteFilterModule {
    public static class ExampleRoute extends StdRoute {
        public ExampleRoute() {
            super("ExampleRoute", new StdRestxRequestMatcher("GET", "/route/filter"));
        }

        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
            resp.getWriter().write("route filter");
        }
    }

    @Provides
    public RestxRoute exampleRoute() {
        return new ExampleRoute();
    }

    @Provides
    public RestxRouteFilter routeFilterExample() {
        return new RestxRouteFilter() {
            @Override
            public Optional<RestxHandlerMatch> match(RestxRoute route) {
                // here we decide to apply the filter or not based on the route class.
                // but we could decide on any route property, for instance relying on the fact that routes generated
                // by @{GET,POST,PUT,DELETE} are always StdEntityRoute instances and their matcher always StdRouteMatcher
                if (route instanceof ExampleRoute) {
                    return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("/route/filter"), new RestxHandler() {
                        @Override
                        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                            resp.getWriter().write(">>");
                            ctx.nextHandlerMatch().handle(req, resp, ctx);
                            resp.getWriter().write("<<");
                        }
                    }));
                } else {
                    return Optional.absent();
                }
            }

        };
    }
}
