package restx.server.simple.simple;

import com.google.common.base.Optional;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.HttpSettings;
import restx.RestxMainRouter;
import restx.RestxMainRouterFactory;
import restx.factory.Factory;
import restx.server.WebServer;
import restx.server.WebServerBase;
import restx.server.WebServerSupplier;
import restx.server.WebServers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 1:34 PM
 */
public abstract class SimpleWebServer extends WebServerBase {
    public static class SimpleWebServerBuilder {
        private int port;
        private String routerPath = "/api";
        private String appBase = null;
        private RestxMainRouter router;
        public SimpleWebServerBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public SimpleWebServerBuilder setRouterPath(String routerPath) {
            this.routerPath = routerPath;
            return this;
        }

        public SimpleWebServerBuilder setAppBase(String appBase) {
            this.appBase = appBase;
            return this;
        }

        public SimpleWebServerBuilder setRouter(RestxMainRouter router) {
            this.router = router;
            return this;
        }

        public SimpleWebServer build() {
            if (router == null) {
                return new SimpleWebServer(routerPath, appBase, port) {
                    @Override
                    protected RestxMainRouter setupRouter() {
                        return RestxMainRouterFactory.newInstance(
                                serverId,
                                Optional.of(WebServers.baseUri("0.0.0.0", port, routerPath)));
                    }

                    @Override
                    public synchronized void stop() throws Exception {
                        super.stop();

                        RestxMainRouterFactory.clear(serverId);
                    }
                };
            } else {
                return new SimpleWebServer(routerPath, appBase, port) {
                    @Override
                    protected RestxMainRouter setupRouter() {
                        return router;
                    }
                };
            }
        }

    }
    public static SimpleWebServerBuilder builder() {
        return new SimpleWebServerBuilder();
    }

    private static final Logger logger = LoggerFactory.getLogger(SimpleWebServer.class);

    private final String routerPath;
    private final HttpSettings httpSettings;

    private RestxMainRouter router;
    private Connection connection;

    private SimpleWebServer(String routerPath, String appBase, int port) {
        super(appBase, port, "localhost", "SimpleFrameowkr", "org.simpleframework", "simple");

        this.routerPath = routerPath;
        this.httpSettings = Factory.getInstance().getComponent(HttpSettings.class);
    }

    public RestxMainRouter getRouter() {
        return router;
    }

    @Override
    protected void _start() throws Exception {
        logger.debug("starting web server");

        router = setupRouter();

        Container container = new Container() {
            @Override
            public void handle(Request request, Response response) {
                try {
                    if (request.getTarget().startsWith(routerPath)) {
                        router.route(
                                new SimpleRestxRequest(httpSettings, routerPath, request), new SimpleRestxResponse(response));
                    } else {
                        response.getPrintStream().print("Not found...");
                        response.getPrintStream().close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Server server = new ContainerServer(container);
        connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(port);

        connection.connect(address);
    }

    protected abstract RestxMainRouter setupRouter();

    @Override
    public void await() {
        // does nothing, simple is started in daemon mode which keeps jvm alive
    }

    @Override
    protected void _stop() throws Exception {
        if (router instanceof AutoCloseable) {
            ((AutoCloseable) router).close();
        }
        connection.close();
        connection = null;
    }

    public static WebServerSupplier simpleWebServerSupplier() {
        return new WebServerSupplier() {
            @Override
            public WebServer newWebServer(int port) {
                return SimpleWebServer.builder().setPort(port).build();
            }
        };
    }
}
