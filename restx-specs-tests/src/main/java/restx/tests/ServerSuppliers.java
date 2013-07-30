package restx.tests;

import restx.server.JettyWebServer;
import restx.server.WebServer;
import restx.server.WebServerSupplier;

/**
 * User: xavierhanin
 * Date: 7/30/13
 * Time: 9:37 PM
 */
public class ServerSuppliers {
    public static WebServerSupplier jettyWebServerSupplier(final String webInfLocation, final String appBase) {
        return new WebServerSupplier() {
            @Override
            public WebServer newWebServer(int port) {
                return new JettyWebServer(webInfLocation, appBase, port, "localhost");
            }
        };
    }
}
