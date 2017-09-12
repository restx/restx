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
    public void should_use_proxy_headers_for_base_uri() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/baseUri")
                .header("Via", "HTTPS/1.1 proxy")
                .header("X-Forwarded-Host", "www.example.com");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("https://www.example.com/api");
    }

    @Test
    public void should_use_proxy_headers_proto_for_base_uri() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/baseUri")
                .header("X-Forwarded-Proto", "https")
                .header("X-Forwarded-Host", "www.example.com");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("https://www.example.com/api");
    }

    @Test
    public void should_access_client_address() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/clientAddress");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).contains("127.0.0.1");
    }

    @Test
    public void should_use_x_forwarded_for_client_address() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/clientAddress").header("X-Forwarded-For", "10.0.10.1");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).contains("10.0.10.1");
    }

    @Test
    public void should_access_request() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/request");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("[RESTX REQUEST] GET /contextParams/request");
    }

    @Test
    public void should_access_response() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/response");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("[RESTX RESPONSE] OK");
    }

    @Test
    public void should_access_locale() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/locale").header("Accept-Language", "fr-FR");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("fr-FR");
    }

    @Test
    public void should_access_locales() throws Exception {
        HttpRequest httpRequest = server.client().GET(
                "/api/contextParams/locales").header("Accept-Language", "fr-FR, en-US");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("[fr_FR, en_US]");
    }
}
