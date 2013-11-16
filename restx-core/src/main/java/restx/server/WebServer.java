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
}
