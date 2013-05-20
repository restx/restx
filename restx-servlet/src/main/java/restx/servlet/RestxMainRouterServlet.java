package restx.servlet;

import restx.RestxMainRouterFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Collection;

public class RestxMainRouterServlet extends AbstractRestxMainRouterServlet {
    public RestxMainRouterServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String baseUri = System.getProperty("restx.baseUri", "");
        String baseServer = config.getServletContext().getInitParameter("restx.baseServerUri");
        if (baseUri.isEmpty() && baseServer != null) {
            Collection<String> mappings = config.getServletContext()
                    .getServletRegistration(config.getServletName()).getMappings();
            if (!mappings.isEmpty()) {
                String routerPath = mappings.iterator().next();
                if (routerPath.endsWith("/*")) {
                    routerPath = routerPath.substring(0, routerPath.length() - 2);
                }
                baseUri = baseServer + routerPath;
            }
        }

        init(RestxMainRouterFactory.newInstance(baseUri));
    }
}
