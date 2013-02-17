package restx.servlet;

import restx.RestxMainRouter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 2:46 PM
 */
public class AbstractRestxMainRouterServlet extends HttpServlet {
    private RestxMainRouter mainRouter;

    public AbstractRestxMainRouterServlet() {
    }

    public AbstractRestxMainRouterServlet(RestxMainRouter mainRouter) {
        this.mainRouter = mainRouter;
    }

    protected void init(RestxMainRouter mainRouter) {
        this.mainRouter = mainRouter;
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
                new HttpServletRestxRequest(req),
                new HttpServletRestxResponse(resp));
    }
}
