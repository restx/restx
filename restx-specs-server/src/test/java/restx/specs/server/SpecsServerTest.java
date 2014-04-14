package restx.specs.server;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.Test;
import restx.server.WebServer;
import restx.server.WebServers;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 12:24 PM
 */
public class SpecsServerTest {
    @Test
    public void should_use_spec() throws Exception {
        WebServer server = SpecsServer.getServer(WebServers.findAvailablePort(), "/api", ".");
        server.start();
        try {
            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/message?who=xavier");

            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("{\"message\":\"hello xavier, it's 14:33:18\"}");
        } finally {
            server.stop();
        }

    }
}
