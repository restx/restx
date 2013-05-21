package restx.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

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
