package samplest.etag;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.HttpTestClient;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 22/5/14
 * Time: 20:43
 */
public class SampleETagResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_provide_etag() throws Exception {
        HttpRequest request = client().GET("/api/etag/test1");
        assertHttpResponse(request, 200, "{%n  \"name\" : \"test1\"%n}");
        assertThat(request.header("ETag")).isEqualTo("5a105e8b9d40e1329780d62ea2265d8a");
    }

    @Test
    public void should_304_on_provided_etag() throws Exception {
        HttpRequest request = client().GET("/api/etag/test1").header("If-None-Match", "5a105e8b9d40e1329780d62ea2265d8a");
        assertThat(request.code()).isEqualTo(304);
    }

    @Test
    public void should_reply_on_bad_provided_etag() throws Exception {
        HttpRequest request = client().GET("/api/etag/test1").header("If-None-Match", "123456");
        assertHttpResponse(request, 200, "{%n  \"name\" : \"test1\"%n}");
    }


    protected HttpRequest assertHttpResponse(HttpRequest httpRequest, int expectedStatus, String expectedBody) {
        assertThat(httpRequest.code()).isEqualTo(expectedStatus);
        assertThat(httpRequest.body().trim()).isEqualTo(String.format(expectedBody));
        return httpRequest;
    }

    protected HttpTestClient client() {
        return server.client().authenticatedAs("admin");
    }
}
