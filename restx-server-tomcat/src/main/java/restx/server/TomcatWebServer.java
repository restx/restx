package restx.server;

import com.google.common.base.Throwables;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TomcatWebServer allow to run embedded tomcat. But its startup time is much slower than JettyWebServer.
 */
public class TomcatWebServer implements WebServer {
    private static final AtomicLong SERVER_ID = new AtomicLong();

    private final Logger logger = LoggerFactory.getLogger(TomcatWebServer.class);

    private final Tomcat tomcat;
    private final int port;
    private final String serverId;

    public TomcatWebServer(String appBase, int port) throws ServletException {
        this.port = port;
        this.serverId = "Tomcat#" + SERVER_ID.incrementAndGet();
        tomcat = new Tomcat();
        tomcat.setPort(port);

        tomcat.setBaseDir(".");
        tomcat.getHost().setAppBase(".");

        String contextPath = "/";

        // Add AprLifecycleListener
        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);

        Context context = tomcat.addWebapp(contextPath, appBase);
        context.getServletContext().setInitParameter("restx.baseServerUri", baseUrl());
        context.getServletContext().setInitParameter("restx.serverId", serverId);
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

    public void start() throws LifecycleException {
        tomcat.start();
    }

    public void startAndAwait() throws LifecycleException {
        start();
        tomcat.getServer().await();
    }

    public void stop() throws LifecycleException {
        tomcat.stop();
    }

    public static WebServerSupplier tomcatWebServerSupplier(final String appBase) {
        return new WebServerSupplier() {
            @Override
            public WebServer newWebServer(int port) {
                try {
                    return new TomcatWebServer(appBase, port);
                } catch (ServletException e) {
                    throw Throwables.propagate(e);
                }
            }
        };
    }

}
