package restx.servlet;

import com.google.common.base.Optional;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.JaninoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxMainRouterFactory;
import restx.factory.Factory;
import restx.server.WebServer;
import restx.server.WebServers;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

public class RestxMainRouterServlet extends AbstractRestxMainRouterServlet {
    public static final String DEPLOYED_SERVER_ID = "SERVLET-ENGINE-1";

    private static final Logger logger = LoggerFactory.getLogger(RestxMainRouterServlet.class);
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
                Collection<String> mappings = getMappings(config);
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
            if (baseServer == null) {
                // restx.baseServerUri is not set, we are most probably deployed on a server, not in embedded mode.
                logger.debug("MINOR: baseUri cannot be found. Define it in restx.baseUri system property\n" +
                        "Note that is has no effect on restx behavior, it's just that it won't be able" +
                        " to properly display the startup banner.");
            } else {
                logger.info("MINOR: baseUri cannot be found. Define it in restx.baseUri system property, or use Servlet 3+ API\n" +
                        "Note that is has no effect on restx behavior, it's just that it won't be able" +
                        " to properly display the startup banner.");
            }
        }

        serverId = Optional.fromNullable(
                config.getServletContext().getInitParameter("restx.serverId"))
                .or(DEPLOYED_SERVER_ID);

        registerIdNeeded(serverId);

        init(RestxMainRouterFactory.newInstance(serverId, baseUri));
    }

    @SuppressWarnings("unchecked")
    protected Collection<String> getMappings(ServletConfig config) throws CompileException, InvocationTargetException {
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(
                "config.getServletContext().getServletRegistration(config.getServletName()).getMappings()",
                Collection.class,
                new String[]{"config"},
                new Class[]{ServletConfig.class});
        return (Collection<String>) expressionEvaluator.evaluate(new Object[]{config});
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
            WebServers.register(new DeployedWebServer(serverId, guessServerType()));
        }
    }

    /**
     * Tries to guess server type based on current callstack.
     *
     * It uses the set of registered server types to make the guess, so this can be contributed through plugins.
     *
     * @return the guessed server type, may be 'unknown'
     */
    private static String guessServerType() {
        try {
            Set<RegisteredServerType> serverTypes = Factory.getInstance().getComponents(RegisteredServerType.class);
            for (StackTraceElement stackTraceElement : new Exception().fillInStackTrace().getStackTrace()) {
                for (RegisteredServerType registeredServerType : serverTypes) {
                    if (stackTraceElement.getClassName().startsWith(registeredServerType.getPackageName())) {
                        return registeredServerType.getServerType();
                    }
                }
            }
            return "unknown";
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private static class DeployedWebServer implements WebServer {
        private final String serverId;
        private final String serverType;

        private DeployedWebServer(String serverId, String serverType) {
            this.serverId = serverId;
            this.serverType = serverType;
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
        public boolean isStarted() {
            return true;
        }

        @Override
        public String baseUrl() {
            return System.getProperty("restx.server.baseUrl", "");
        }

        @Override
        public String getServerId() {
            return serverId;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public String getServerType() {
            return serverType;
        }
    }

}
