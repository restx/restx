package restx.server;

import com.google.common.eventbus.EventBus;

/**
 * User: xavierhanin
 * Date: 1/16/13
 * Time: 9:46 PM
 */
public interface WebServer {
    public void start() throws Exception;
    public void startAndAwait() throws Exception;
    public void stop() throws Exception;
    public String baseUrl();
    public String getServerId();
    public int getPort();

    /**
     * Returns an eventBus associated with this server instance, which can be used to post events
     * and register to event publications.
     *
     * The event bus is private to the server instance, and has the same lifetime as the server.
     * So make sure to unregister your listeners on this eventbus if you don't want to keep a reference
     * on them for the whole lifetime of the server.
     *
     * Inside components par of a restx app, you usually get the event bus injected rather than accessing it
     * with this method.
     *
     * @return an EventBus associated with this server instance.
     */
    public EventBus getEventBus();
}
