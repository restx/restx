package restx.specs.server;

import restx.server.WebServer;
import restx.server.simple.simple.SimpleWebServer;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 12:02 PM
 */
public class SpecsServer {
    public static void main(String[] args) throws Exception {
        int port = 8888;
        String routerPath = "/api";
        String appBase = ".";
        System.setProperty("restx.factory.load", "onrequest");
        getServer(port, routerPath, appBase).startAndAwait();
    }

    static WebServer getServer(int port, String routerPath, String appBase) {
        return new SimpleWebServer(routerPath, appBase, port);
    }
}
