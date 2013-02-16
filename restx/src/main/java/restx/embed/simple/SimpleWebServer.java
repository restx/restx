package restx.embed.simple;

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
import restx.embed.WebServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 1:34 PM
 */
public class SimpleWebServer implements WebServer {
    private final Logger logger = LoggerFactory.getLogger(SimpleWebServer.class);

    private String routerPath;
    private final String appBase;
    private final int port;
    private Connection connection;
    private RestxMainRouter router;

    public SimpleWebServer(String appBase, int port) {
        this("/api", appBase, port);
    }

    public SimpleWebServer(String routerPath, String appBase, int port) {
        this.routerPath = routerPath;
        this.appBase = appBase;
        this.port = port;
        this.router = new RestxMainRouter();
    }

    public RestxMainRouter getRouter() {
        return router;
    }

    @Override
    public void start() throws Exception {
        logger.info("starting web server");

        Container container = new Container() {
            @Override
            public void handle(Request request, Response response) {
                try {
                    if (request.getPath().getPath().startsWith(routerPath)) {
                        router.route(getFactoryContextName(port),
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
        router.init();
    }

    public static String getFactoryContextName(int port) {
        return String.format("RESTX@%s", port);
    }

    @Override
    public void startAndAwait() throws Exception {
        start();
        // this doesn't await but since the simple thread is not a daemon thread it will keep the jvm alive
    }

    @Override
    public void stop() throws Exception {
        router.close();
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
