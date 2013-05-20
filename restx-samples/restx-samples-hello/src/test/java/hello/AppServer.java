package hello;

import restx.server.WebServer;
import restx.server.simple.simple.SimpleWebServer;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 5:10 PM
 */
public class AppServer {
    public static final String WEB_INF_LOCATION = "src/main/webapp/WEB-INF/web.xml";
    public static final String WEB_APP_LOCATION = "src/main/webapp";

    public static void main(String[] args) throws Exception {
        WebServer server = new SimpleWebServer(WEB_APP_LOCATION, 8086);
        server.startAndAwait();
    }
}
