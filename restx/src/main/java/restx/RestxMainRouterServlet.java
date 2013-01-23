package restx;

import com.google.common.collect.ImmutableList;
import dagger.ObjectGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ServiceLoader;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 2:46 PM
 */
public class RestxMainRouterServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(RestxMainRouterServlet.class);

    private ObjectGraph objectGraph;

    private RestxRouter mainRouter;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        logger.info("locating restx router modules...");
        ImmutableList<RestxRouterModule> restxRouterModules = ImmutableList.copyOf(ServiceLoader.load(RestxRouterModule.class));
        logger.info("restx router modules: {}", restxRouterModules);

        objectGraph = ObjectGraph.create(restxRouterModules.toArray());

        ImmutableList.Builder<RestxRoute> routersBuilder = ImmutableList.builder();
        for (RestxRouterModule restxRouterModule : restxRouterModules) {
            routersBuilder.add(objectGraph.get(restxRouterModule.router()));
        }
        mainRouter = new RestxRouter("MainRouter", routersBuilder.build());
        logger.info("restx main router servlet ready: " + mainRouter);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("incoming request {}", req);

        if (!mainRouter.route(req, resp)) {
            logger.info("no route found for {}\n" +
                    "routes:\n" +
                    "-----------------------------------\n" +
                    "{}\n" +
                    "-----------------------------------", req, mainRouter);
            resp.setStatus(404);
        }
    }
}
