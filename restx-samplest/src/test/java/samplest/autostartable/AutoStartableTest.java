package samplest.autostartable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import org.junit.Test;
import restx.factory.*;
import restx.server.WebServers;
import restx.server.simple.simple.SimpleWebServer;
import samplest.autostartable.AutoStartableTestComponent;
import samplest.autostartable.AutoStartableTestRoute;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 27/11/13
 * Time: 21:24
 */
public class AutoStartableTest {
    @Test
    public void should_handle_auto_startable_in_dev_mode() throws Exception {
        System.setProperty("restx.mode", "dev");

        SimpleWebServer server = SimpleWebServer.builder()
                .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
        server.start();
        try {
            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/test");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo(
                    "called: 1 - autostartable: called: 1 started: 1 closed: 0 instanciated: 1");

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test");
            assertThat(httpRequest.code()).isEqualTo(200);
            // called should be only one in test mode, components are dropped at each request
            // but autostartable should be reused
            assertThat(httpRequest.body().trim()).isEqualTo(
                    "called: 1 - autostartable: called: 2 started: 1 closed: 0 instanciated: 1");
        } finally {
            server.stop();
        }
    }
}
