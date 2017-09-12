package restx.server;

import com.google.common.base.Strings;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MoreFiles.checkFileExists;

public class Jetty7WebServer extends WebServerBase {
    private static final Logger logger = LoggerFactory.getLogger(Jetty7WebServer.class);

    private Server server;
    private String webInfLocation;

    public Jetty7WebServer(String appBase, int aPort) {
        this(null, appBase, aPort, null);
    }

    public Jetty7WebServer(String webInfLocation, String appBase, int port, String bindInterface) {
        super(checkNotNull(appBase), port, bindInterface, "Jetty7", "org.eclipse.jetty", "jetty-server");

        if (webInfLocation != null) {
            checkFileExists(webInfLocation);
        }
        this.webInfLocation = webInfLocation;
    }

    @Override
    protected void _start() throws Exception {
        server = new Server();

        server.setThreadPool(createThreadPool());
        server.addConnector(createConnector());
        server.setHandler(createHandlers(createContext()));
        server.setStopAtShutdown(true);

        server.start();
    }

    @Override
    public void await() throws InterruptedException {
        server.join();
    }

    @Override
    protected void _stop() throws Exception {
        server.stop();
        server = null;
    }

    protected ThreadPool createThreadPool() {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(1);
        threadPool.setMaxThreads(Math.max(10, Runtime.getRuntime().availableProcessors()));
        return threadPool;
    }

    protected SelectChannelConnector createConnector() {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setHost(bindInterface);
        return connector;
    }

    protected HandlerCollection createHandlers(WebAppContext webAppContext) {

        HandlerList contexts = new HandlerList();
        contexts.setHandlers(new Handler[]{webAppContext});

        HandlerCollection result = new HandlerCollection();
        result.setHandlers(new Handler[]{contexts});

        return result;
    }

    protected WebAppContext createContext() {
        final WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/");
        ctx.setWar(appBase);
        if(!Strings.isNullOrEmpty(webInfLocation)) {
            ctx.setDescriptor(webInfLocation);
        }
        // configure security to avoid err println "Null identity service, trying login service:"
        // but I've found no way to get rid of LoginService=xxx log on system err :(
        HashLoginService loginService = new HashLoginService();
        loginService.setIdentityService(new DefaultIdentityService());
        ctx.getSecurityHandler().setLoginService(loginService);
        ctx.getSecurityHandler().setIdentityService(loginService.getIdentityService());

        ctx.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                ctx.getServletContext().setInitParameter("restx.baseServerUri", baseUrl());
                ctx.getServletContext().setInitParameter("restx.serverId", getServerId());
            }
        });

        return ctx;
    }

    public static WebServerSupplier jettyWebServerSupplier(final String webInfLocation, final String appBase) {
        return new WebServerSupplier() {
            @Override
            public WebServer newWebServer(int port) {
                return new Jetty7WebServer(webInfLocation, appBase, port, "0.0.0.0");
            }
        };
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("usage: jetty-run <appbase> [<port>]");
            System.exit(1);
        }

        String appBase = args[0];
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8086;
        new Jetty7WebServer(appBase + "WEB-INF/web.xml", appBase, port, "0.0.0.0").startAndAwait();
    }
}
