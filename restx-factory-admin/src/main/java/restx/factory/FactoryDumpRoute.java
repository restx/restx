package restx.factory;

import restx.*;

import javax.inject.Inject;
import java.io.IOException;

@Component
public class FactoryDumpRoute extends StdRoute {
    private final Factory factory;

    @Inject
    public FactoryDumpRoute(Factory factory) {
        super("FactoryRoute", new StdRestxRequestMatcher("GET", "/@/factory"));
        this.factory = factory;
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println(factory.dump());
    }
}
