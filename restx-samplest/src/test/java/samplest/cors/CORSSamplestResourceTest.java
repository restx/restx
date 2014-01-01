package samplest.cors;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.HttpTestClient;
import restx.tests.RestxServerRule;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 28/12/13
 * Time: 10:21
 */
public class CORSSamplestResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    private final String randomOrigin = "http://localhost:" + Math.abs(new Random().nextInt());


    @Test
    public void should_handle_no_origin_request() throws Exception {
        assertHttpResponse(client().GET("/api/cors/1"), 200, "CORS1");
    }

    @Test
    public void should_handle_good_origin_on_get() throws Exception {
        HttpRequest httpRequest = client().GET("/api/cors/1").header("Origin", "http://localhost:9000");
        assertHttpResponse(httpRequest, 200, "CORS1");
        assertThat(httpRequest.header("Access-Control-Allow-Origin")).isEqualTo("http://localhost:9000");
    }

    @Test
    public void should_handle_good_origin_on_post() throws Exception {
        HttpRequest httpRequest = client().POST("/api/cors/1").header("Origin", "http://localhost:9000").send("{}");
        assertHttpResponse(httpRequest, 200, "CORS1");
        assertThat(httpRequest.header("Access-Control-Allow-Origin")).isEqualTo("http://localhost:9000");
    }

    @Test
    public void should_reject_invalid_origin_on_get() throws Exception {
        HttpRequest httpRequest = client().GET("/api/cors/1").header("Origin", "http://localhost:80");
        assertHttpResponse(httpRequest, 403, "");
    }

    @Test
    public void should_accept_same_origin_on_get() throws Exception {
        HttpRequest httpRequest = client().GET("/api/cors/1")
                .header("Origin", server.getServer().baseUrl());
        assertHttpResponse(httpRequest, 200, "CORS1");
    }

    @Test
    public void should_reject_invalid_origin_on_post() throws Exception {
        HttpRequest httpRequest = client().POST("/api/cors/1").header("Origin", "http://localhost:80").send("{}");
        assertHttpResponse(httpRequest, 403, "");
    }

    @Test
    public void should_reject_head_on_cors1() throws Exception {
        HttpRequest httpRequest = client().HEAD("/api/cors/1").header("Origin", "http://localhost:9000");
        assertHttpResponse(httpRequest, 403, "");
    }

    @Test
    public void should_handle_any_origin_on_get() throws Exception {
        HttpRequest httpRequest = client().GET("/api/cors/2").header("Origin", randomOrigin);
        assertHttpResponse(httpRequest, 200, "CORS2");
        assertThat(httpRequest.header("Access-Control-Allow-Origin")).isEqualTo(randomOrigin);
    }

    @Test
    public void should_handle_any_origin_on_post() throws Exception {
        HttpRequest httpRequest = client().POST("/api/cors/2").header("Origin", randomOrigin).send("{}");
        assertHttpResponse(httpRequest, 200, "CORS2");
        assertThat(httpRequest.header("Access-Control-Allow-Origin")).isEqualTo(randomOrigin);
    }

    @Test
    public void should_handle_any_origin_on_head() throws Exception {
        HttpRequest httpRequest = client().HEAD("/api/cors/2").header("Origin", randomOrigin);
        assertHttpResponse(httpRequest, 204, "");
        assertThat(httpRequest.header("Access-Control-Allow-Origin")).isEqualTo(randomOrigin);
    }

    @Test
    public void should_reject_preflight_request_for_put_when_not_configured() throws Exception {
        HttpRequest httpRequest = client().OPTIONS("/api/cors/2")
                .header("Origin", randomOrigin)
                .header("Access-Control-Request-Method", "PUT")
                ;
        assertHttpResponse(httpRequest, 403, "");
    }

    @Test
    public void should_handle_preflight_request_for_put() throws Exception {
        HttpRequest httpRequest = client().OPTIONS("/api/cors/3")
                .header("Origin", randomOrigin)
                .header("Access-Control-Request-Method", "PUT")
                ;
        assertHttpResponse(httpRequest, 200, "");
        assertThat(httpRequest.header("Access-Control-Allow-Origin")).isEqualTo(randomOrigin);
    }

    @Test
    public void should_reject_preflight_request_for_delete_when_not_configured() throws Exception {
        HttpRequest httpRequest = client().OPTIONS("/api/cors/3")
                .header("Origin", randomOrigin)
                .header("Access-Control-Request-Method", "DELETE")
                ;
        assertHttpResponse(httpRequest, 403, "");
    }

    protected HttpRequest assertHttpResponse(HttpRequest httpRequest, int expectedStatus, String expectedBody) {
        assertThat(httpRequest.code()).isEqualTo(expectedStatus);
        assertThat(httpRequest.body().trim()).isEqualTo(expectedBody);
        return httpRequest;
    }


    protected HttpTestClient client() {
        return server.client().authenticatedAs("admin");
    }
}
