package restx.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import restx.HttpSettings;
import restx.RestxMainRouter;
import restx.factory.Factory;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 2:46 PM
 */
public class AbstractRestxMainRouterServlet extends HttpServlet {
    private RestxMainRouter mainRouter;
    private HttpSettings httpSettings;

    public AbstractRestxMainRouterServlet() {
    }

    public AbstractRestxMainRouterServlet(RestxMainRouter mainRouter) {
        this.mainRouter = mainRouter;
    }

    protected void init(RestxMainRouter mainRouter) {
        this.mainRouter = mainRouter;
        httpSettings = Factory.getInstance().getComponent(HttpSettings.class);
    }

    @Override
    public void destroy() {
        try {
            if (mainRouter instanceof AutoCloseable) {
                ((AutoCloseable) mainRouter).close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.destroy();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        mainRouter.route(
                new HttpServletRestxRequest(httpSettings, req),
                new HttpServletRestxResponse(resp, req));
    }
}
