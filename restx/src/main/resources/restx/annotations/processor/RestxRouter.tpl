package {package};

import restx.RestxRoute;
import restx.RestxRouter;
import restx.RouteLifecycleListener;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class {router} implements RestxRoute {
{injectRoutes}

    private RestxRouter buildRouter() {
        return new RestxRouter(
                "{router}",
{routes}
        );
    }

    private volatile RestxRouter router;

    @Override
    public boolean route(HttpServletRequest req, HttpServletResponse resp, RouteLifecycleListener listener) throws IOException {
        return router().route(req, resp, listener);
    }

    private RestxRouter router() {
        // this is not thread safe, but it's ok to construct multiple routers,
        // all behave the same and they are stateless
        if (router == null) {
            router = buildRouter();
        }
        return router;
    }

    @Override
    public String toString() {
        return router().toString();
    }
}
