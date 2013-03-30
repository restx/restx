package restx.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * User: xavierhanin
 * Date: 1/16/13
 * Time: 9:48 PM
 */
public class WebServers {
    public static int findAvailablePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}
