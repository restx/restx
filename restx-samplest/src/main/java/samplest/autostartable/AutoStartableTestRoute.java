package samplest.autostartable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.factory.Component;

import java.io.IOException;

/**
* Date: 1/12/13
* Time: 14:21
*/
@Component
public class AutoStartableTestRoute extends StdRoute {
    private static final Logger logger = LoggerFactory.getLogger(AutoStartableTestRoute.class);

    private final AutoStartableTestComponent c;
    private int called;

    public AutoStartableTestRoute(AutoStartableTestComponent c) {
        super("AutoStartableTestRoute", new StdRestxRequestMatcher("GET", "/autostartable/test"));
        this.c = c;
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        try {
            c.call();
            resp.setContentType("text/plain");
            resp.getWriter().println("called: " + ++called + " - autostartable: called: " + c.getCalled()
                    +" started: " + AutoStartableTestComponent.getStarted()
                    + " closed: " + AutoStartableTestComponent.getClosed()
                    + " instanciated: " + AutoStartableTestComponent.getInstanciated()
                    + " serverId: " + c.getServerId()
                    + " baseUrl: " + c.getBaseUrl()
                    + " routerPresent: " + c.getRouter().isPresent()
            );
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }
}
