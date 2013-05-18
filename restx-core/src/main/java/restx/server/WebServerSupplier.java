package restx.server;

/**
 * User: xavierhanin
 * Date: 5/18/13
 * Time: 11:32 AM
 */
public interface WebServerSupplier {
    public WebServer newWebServer(int port);
}
