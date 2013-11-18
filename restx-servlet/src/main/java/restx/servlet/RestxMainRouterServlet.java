package restx.servlet;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.JaninoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxMainRouterFactory;
import restx.server.WebServer;
import restx.server.WebServers;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class RestxMainRouterServlet extends AbstractRestxMainRouterServlet {
    public static final String DEPLOYED_SERVER_ID = "SERVLET-ENGINE-1";

    private final Logger logger = LoggerFactory.getLogger(RestxMainRouterServlet.class);
    private String serverId;

    public RestxMainRouterServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        Optional<String> baseUri = Optional.fromNullable(System.getProperty("restx.baseUri"));
        String baseServer = config.getServletContext().getInitParameter("restx.baseServerUri");
        if (!baseUri.isPresent() && baseServer != null) {
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
                    baseUri = Optional.of(baseServer + routerPath);
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
        if (!baseUri.isPresent()) {
            logger.info("MINOR: baseUri cannot be found. Define it in restx.baseUri system property, or use Servlet 3+ API\n" +
                    "Note that is has no effect on restx behavior, it's just that it won't be able" +
                    " to properly display the startup banner.");
        }

        serverId = Optional.fromNullable(
                config.getServletContext().getInitParameter("restx.serverId"))
                .or(DEPLOYED_SERVER_ID);

        registerIdNeeded(serverId);

        init(RestxMainRouterFactory.newInstance(serverId, baseUri));
    }

    @Override
    public void destroy() {
        super.destroy();

        if (serverId != null) {
            RestxMainRouterFactory.clear(serverId);
        }
    }

    private static synchronized void registerIdNeeded(String serverId) {
        Optional<WebServer> serverById = WebServers.getServerById(serverId);
        if (!serverById.isPresent()) {
            WebServers.register(new DeployedWebServer(serverId));
        }
    }

    private static class DeployedWebServer implements WebServer {
        private final String serverId;

        private DeployedWebServer(String serverId) {
            this.serverId = serverId;
        }

        @Override
        public void start() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void startAndAwait() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void await() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public String baseUrl() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getServerId() {
            return serverId;
        }

        @Override
        public int getPort() {
            throw new UnsupportedOperationException();
        }
    }
}
