package samplest.jacksonviews;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.Test;
import restx.server.WebServers;
import restx.server.simple.simple.SimpleWebServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: eoriou
 * Date: 04/12/2013
 * Time: 15:52
 */
public class JacksonViewsTest {

    @Test
    public void should_retrieve_well_formed_data() throws Exception {

        SimpleWebServer server = SimpleWebServer.builder()
                .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
        server.start();
        try {

            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/jacksonviews/cars");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo(
                    "[\"{'status' : 'ok'}\",\"{'status' : 'ok'}\",\"{'status' : 'ok'}\",\"{'status' : 'ok'}\"]");

        } finally {
            server.stop();
        }
    }
}
