package restx.factory;

import restx.*;

import javax.inject.Inject;
import java.io.IOException;

@Component
public class FactoryDumpRoute extends StdRoute {
    private final Factory factory;

    @Inject
    public FactoryDumpRoute(Factory factory) {
        super("FactoryRoute", new StdRouteMatcher("GET", "/@/factory"));
        this.factory = factory;
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setContentType("text/html");
        resp.getWriter().println("<pre>" + factory.dump() + "</pre>");
    }
}
