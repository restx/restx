package restx.common;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Date: 16/11/13
 * Time: 12:59
 */
public class MoreIO {
    public static void checkCanOpenSocket(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.close();
        } catch (IOException e) {
            if (e.getMessage().equals("Address already in use")) {
                throw new IllegalStateException("can't open socket on port " + port + ": There is already a server listening on this port." +
                        "\n\t\t>> Maybe another instance of this server is already started?" +
                        "\n\t\t>> Double check your running java process if in doubt.");
            } else {
                throw new IllegalStateException("can't open socket on port " + port + ". Reason: " + e.getMessage(), e);
            }
        }
    }
}
