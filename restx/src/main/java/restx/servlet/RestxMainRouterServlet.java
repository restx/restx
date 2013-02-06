package restx.servlet;

import restx.RestxMainRouter;
import restx.servlet.HttpServletRestxRequest;
import restx.servlet.HttpServletRestxResponse;

import javax.servlet.ServletConfig;
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
public class RestxMainRouterServlet extends HttpServlet {
    private final RestxMainRouter mainRouter = new RestxMainRouter();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        mainRouter.init();
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        mainRouter.route(getFactoryContextName(req.getServerPort()),
                new HttpServletRestxRequest(req),
                new HttpServletRestxResponse(resp));
    }

    public static String getFactoryContextName(int port) {
        return String.format("RESTX@%s", port);
    }
}
