package restx.servlet;

import restx.RestxMainRouterFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 2:46 PM
 */
public class RestxMainRouterServlet extends AbstractRestxMainRouterServlet {
    private final RestxMainRouterFactory mainRouter = new RestxMainRouterFactory();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        mainRouter.init();

        init(mainRouter);
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        mainRouter.setContextName(getFactoryContextName(req.getServerPort()));
        super.service(req, resp);
    }

    public static String getFactoryContextName(int port) {
        return String.format("RESTX@%s", port);
    }
}
