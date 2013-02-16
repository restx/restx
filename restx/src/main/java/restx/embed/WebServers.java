package restx.embed;

import restx.embed.simple.SimpleWebServer;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * User: xavierhanin
 * Date: 1/16/13
 * Time: 9:48 PM
 */
public class WebServers {
    public static WebServer newWebServer(String webInfLocation, String appBase, int port) throws Exception {
        String server = System.getProperty("server", "jetty");
        switch (server) {
            case "jetty":
                return new JettyWebServer(webInfLocation, appBase, port, null);
            case "tomcat":
                return new TomcatWebServer(appBase, port);
            case "simple":
                return new SimpleWebServer(appBase, port);
            default:
                throw new IllegalStateException("unknown server " + server + "." +
                        " Review your configuration with -Dserver={jetty,tomcat}");
        }
    }

    public static int findAvailablePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}
