package restx.server;

import com.google.common.base.Optional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: xavierhanin
 * Date: 1/16/13
 * Time: 9:48 PM
 */
public class WebServers {

    private static final ConcurrentMap<String, WebServer> servers = new ConcurrentHashMap<>();

    /**
     * Registers a WebServer instance.
     *
     * This should be used by WebServer impl only. Make sure to call unregister when server is stopped.
     *
     * @param server
     */
    public static void register(WebServer server) {
        servers.putIfAbsent(server.getServerId(), server);
    }

    /**
     * Unregisters a previsouly registered server by id.
     *
     * @param serverId
     */
    public static void unregister(String serverId) {
        servers.remove(serverId);
    }

    /**
     * Returns a currently registered server by id.
     *
     * Servers are registered once startup, and unregistered when stopped.
     *
     * @param serverId
     * @return the Optional WebServer associated with that id.
     */
    public static Optional<WebServer> getServerById(String serverId) {
        return Optional.fromNullable(servers.get(serverId));
    }

    public static int findAvailablePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    public static String baseUri(String bindInterface, int port) {
        if (bindInterface == null || bindInterface.equals("0.0.0.0")) {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                bindInterface = ip.getHostAddress();
            } catch (UnknownHostException e) {
                bindInterface = "localhost";
            }
        }
        return String.format("http://%s:%s", bindInterface, port);
    }

    public static String baseUri(String bindInterface, int port, String routerPath) {
        return baseUri(bindInterface, port) + routerPath;
    }
}
