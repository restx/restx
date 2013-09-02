package restx.server;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
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

import java.util.concurrent.atomic.AtomicLong;

public class JettyWebServer implements WebServer {
    private static final AtomicLong SERVER_ID = new AtomicLong();

    private final Logger logger = LoggerFactory.getLogger(JettyWebServer.class);

    private Server server;
    private int port;
    private String bindInterface;
    private String appBase;
    private String webInfLocation;
    private String serverId;
    private final EventBus eventBus = new EventBus();


    public JettyWebServer(String appBase, int aPort) {
        this(null, appBase, aPort, null);
    }

    public JettyWebServer(String webInfLocation, String appBase, int port, String bindInterface) {
        this.port = port;
        this.bindInterface = bindInterface;
        this.appBase = appBase;
        this.webInfLocation = webInfLocation;
        this.serverId = "Jetty#" + SERVER_ID.incrementAndGet();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public String getServerId() {
        return serverId;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String baseUrl() {
        return WebServers.baseUri("localhost", port);
    }

    public void start() throws Exception {
        server = new Server();
        WebServers.register(this);

        server.setThreadPool(createThreadPool());
        server.addConnector(createConnector());
        server.setHandler(createHandlers(createContext()));
        server.setStopAtShutdown(true);

        server.start();
    }

    public void startAndAwait() throws Exception {
        start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
        WebServers.unregister(serverId);
    }

    private ThreadPool createThreadPool() {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(1);
        threadPool.setMaxThreads(10);
        return threadPool;
    }

    private SelectChannelConnector createConnector() {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setHost(bindInterface);
        return connector;
    }

    private HandlerCollection createHandlers(WebAppContext webAppContext) {

        HandlerList contexts = new HandlerList();
        contexts.setHandlers(new Handler[]{webAppContext});

        HandlerCollection result = new HandlerCollection();
        result.setHandlers(new Handler[]{contexts});

        return result;
    }

    private WebAppContext createContext() {
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
                return new JettyWebServer(webInfLocation, appBase, port, "0.0.0.0");
            }
        };
    }
}
