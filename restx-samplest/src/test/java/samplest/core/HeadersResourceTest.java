package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.joda.time.DateTime;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class HeadersResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_return_expire_header() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/headers/expires");
        assertThat(httpRequest.code()).isEqualTo(200);

        long duration = Math.abs(httpRequest.expires() - DateTime.now().plusDays(2).plusHours(4).getMillis());
        assertThat(duration).isLessThan(5000);
    }

    @Test
    public void should_return_location_header_with_current_uri() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .POST("/api/headers/foos")
                .send("{\"name\":\"FOO\"}");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.location()).isEqualTo(server.getServer().baseUrl()+"/api/headers/foos/123456");
    }

    @Test
    public void should_return_location_header_with_base_uri() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .POST("/api/headers/foos2")
                .send("{\"name\":\"FOO\"}");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.location()).isEqualTo(server.getServer().baseUrl()+"/api/headers/foos2/123456");
    }
}
