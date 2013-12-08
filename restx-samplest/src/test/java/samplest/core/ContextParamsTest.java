package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.factory.Factory.LocalMachines.overrideComponents;
import static restx.factory.Factory.LocalMachines.threadLocal;
import static restx.tests.HttpTestClient.GET;

/**
 * @see samplest.core.ContextParamsResource
 */
public class ContextParamsTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_access_base_uri() throws Exception {
        HttpRequest httpRequest = GET(
                server.getServer().baseUrl() + "/api/contextParams/baseUri");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(server.getServer().baseUrl() + "/api");
    }

    @Test
    public void should_access_client_address() throws Exception {
        HttpRequest httpRequest = GET(
                server.getServer().baseUrl() + "/api/contextParams/clientAddress");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).contains("127.0.0.1");
    }

    @Test
    public void should_access_request() throws Exception {
        HttpRequest httpRequest = GET(
                server.getServer().baseUrl() + "/api/contextParams/request");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("[RESTX REQUEST] GET /contextParams/request");
    }
}
