package restx.server.simple.simple;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxMainRouter;
import restx.RestxMainRouterFactory;
import restx.server.WebServer;
import restx.server.WebServers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 1:34 PM
 */
public abstract class SimpleWebServer implements WebServer {
    private static final AtomicLong SERVER_ID = new AtomicLong();
    public static class SimpleWebServerBuilder {

        private int port;
        private String routerPath = "/api";
        private String appBase = null;
        private String serverId;
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

        public SimpleWebServerBuilder setServerId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        public SimpleWebServerBuilder setRouter(RestxMainRouter router) {
            this.router = router;
            return this;
        }

        public SimpleWebServer build() {
            if (serverId == null) {
                serverId = "SimpleWebServer#" + SERVER_ID.incrementAndGet();
            }

            if (router == null) {
                return new SimpleWebServer(serverId, routerPath, appBase, port) {
                    @Override
                    protected RestxMainRouter setupRouter() {
                        return RestxMainRouterFactory.newInstance(
                                                    serverId,
                                                    WebServers.baseUri("0.0.0.0", port, routerPath));
                    }
                };
            } else {
                return new SimpleWebServer(serverId, routerPath, appBase, port) {
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

    private final Logger logger = LoggerFactory.getLogger(SimpleWebServer.class);

    private final String serverId;

    private final String routerPath;
    private final String appBase;
    private final int port;

    private RestxMainRouter router;
    private Connection connection;

    private SimpleWebServer(String serverId, String routerPath, String appBase, int port) {
        this.serverId = serverId;
        this.routerPath = routerPath;
        this.appBase = appBase;
        this.port = port;
    }

    public String getServerId() {
        return serverId;
    }

    public RestxMainRouter getRouter() {
        return router;
    }

    @Override
    public synchronized void start() throws Exception {
        logger.debug("starting web server");

        router = setupRouter();

        Container container = new Container() {
            @Override
            public void handle(Request request, Response response) {
                try {
                    if (request.getTarget().startsWith(routerPath)) {
                        router.route(
                                new SimpleRestxRequest(routerPath, request), new SimpleRestxResponse(response));
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
    public void startAndAwait() throws Exception {
        start();
        // this doesn't await but since the simple thread is not a daemon thread it will keep the jvm alive
    }

    @Override
    public synchronized void stop() throws Exception {
        if (router instanceof AutoCloseable) {
            ((AutoCloseable) router).close();
        }
        connection.close();
    }

    @Override
    public String baseUrl() {
        return String.format("http://localhost:%s", port);
    }

    @Override
    public int getPort() {
        return port;
    }

}
