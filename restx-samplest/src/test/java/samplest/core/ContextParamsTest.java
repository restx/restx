package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see samplest.core.ContextParamsResource
 */
public class ContextParamsTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_access_base_uri() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/baseUri");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(server.getServer().baseUrl() + "/api");
    }

    @Test
    public void should_access_client_address() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/clientAddress");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).contains("127.0.0.1");
    }

    @Test
    public void should_access_request() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/request");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("[RESTX REQUEST] GET /contextParams/request");
    }

    @Test
    public void should_access_locale() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/locale").header("Accept-Language", "fr-FR");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("fr-FR");
    }
}
