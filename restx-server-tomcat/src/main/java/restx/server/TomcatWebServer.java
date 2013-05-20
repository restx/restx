package restx.server;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

/**
 * TomcatWebServer allow to run embedded tomcat. But its startup time is much slower than JettyWebServer.
 */
public class TomcatWebServer implements WebServer {
    private final Logger logger = LoggerFactory.getLogger(TomcatWebServer.class);

    private final Tomcat tomcat;
    private final int port;

    public TomcatWebServer(String appBase, int port) throws ServletException {
        this.port = port;
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
    }


    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String baseUrl() {
        return String.format("http://localhost:%s", port);
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

}
