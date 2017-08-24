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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TomcatWebServer allow to run embedded tomcat. But its startup time is much slower than JettyWebServer.
 */
public class TomcatWebServer extends WebServerBase {
    private final static Logger logger = LoggerFactory.getLogger(TomcatWebServer.class);

    private final Tomcat tomcat;
    private final Context context;

    public TomcatWebServer(String appBase, int port) throws ServletException {
        super(checkNotNull(appBase), port, "localhost", "Apache Tomcat", "org.apache.tomcat", "tomcat-catalina");

        tomcat = new Tomcat();

        tomcat.setPort(port);
        tomcat.setBaseDir(".");
        tomcat.getHost().setAppBase(".");

        String contextPath = "/";

        // Add AprLifecycleListener
        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);

        context = tomcat.addWebapp(contextPath, appBase);
    }

    @Override
    protected void _start() throws LifecycleException {
        context.getServletContext().setInitParameter("restx.baseServerUri", baseUrl());
        context.getServletContext().setInitParameter("restx.serverId", serverId);

        tomcat.start();
    }

    @Override
    public void await() {
        tomcat.getServer().await();
    }

    protected void _stop() throws LifecycleException {
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
