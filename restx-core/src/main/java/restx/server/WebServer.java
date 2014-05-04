package restx.server;

/**
 * User: xavierhanin
 * Date: 1/16/13
 * Time: 9:46 PM
 */
public interface WebServer {
    public void start() throws Exception;
    public void await() throws Exception;
    public void startAndAwait() throws Exception;
    public void stop() throws Exception;

    /**
     * Returns the base URL on which this server is listening.
     *
     * This is useful to build a URL to make a call on that server. This is not manadatory that this base URL can be
     * used on another machine than the current machine.
     *
     * May return an empty string if unknown.
     *
     * Eg: http://localhost:8080
     *
     * @return the base URL on which this server is listening, or an empty string if unkown.
     */
    public String baseUrl();

    /**
     * The id of this server, which is unique in the current JVM classloader.
     *
     * WebServer are usually registered using their id so that they can be found with WebServers.getServerById(id).
     *
     * @return this web server id.
     */
    public String getServerId();

    /**
     * Returns the port on which this server is listening.
     *
     * It this information is not known or cannot be disclosed, return 0.
     *
     * @return the port on which this server is listening, or 0 if unknown.
     */
    public int getPort();

    public boolean isStarted();
}
