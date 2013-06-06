package restx.servlet;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.JaninoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxMainRouterFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class RestxMainRouterServlet extends AbstractRestxMainRouterServlet {
    private final Logger logger = LoggerFactory.getLogger(RestxMainRouterServlet.class);

    public RestxMainRouterServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String baseUri = System.getProperty("restx.baseUri", "");
        String baseServer = config.getServletContext().getInitParameter("restx.baseServerUri");
        if (baseUri.isEmpty() && baseServer != null) {
            try {
                // try to use servlet3 API without actually requiring it
                ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(
                        "config.getServletContext().getServletRegistration(config.getServletName()).getMappings()",
                        Collection.class,
                        new String[]{"config"},
                        new Class[]{ServletConfig.class});
                Collection<String> mappings = (Collection<String>) expressionEvaluator.evaluate(new Object[]{config});
                if (!mappings.isEmpty()) {
                    String routerPath = mappings.iterator().next();
                    if (routerPath.endsWith("/*")) {
                        routerPath = routerPath.substring(0, routerPath.length() - 2);
                    }
                    baseUri = baseServer + routerPath;
                    logger.debug("deduced baseUri from servlet registration: {}", baseUri);
                }
            } catch (JaninoRuntimeException e) {
                logger.info("servlet <3 detected. use servlet3+ to get automatic baseUri detection");
            } catch (CompileException e) {
                logger.info("servlet <3 detected. use servlet3+ to get automatic baseUri detection");
            } catch (InvocationTargetException e) {
                logger.info("servlet <3 detected. use servlet3+ to get automatic baseUri detection");
            }
        }
        if (baseUri.isEmpty()) {
            logger.info("baseUri cannot be found. Define it in restx.baseUri system property, or use Servlet 3+ API");
        }

        String serverId = config.getServletContext().getInitParameter("restx.serverId");

        init(RestxMainRouterFactory.newInstance(serverId, baseUri));
    }
}
