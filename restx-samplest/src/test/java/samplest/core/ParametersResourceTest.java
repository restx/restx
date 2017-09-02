package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 13/12/13
 * Time: 23:59
 */
public class ParametersResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_return_path_params() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/params/path/v1/v2/35v4");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=v1 b=v2 c=35 d=v4");
    }

    @Test
    public void should_return_header_params() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/params/headers")
                .header("X-A", "aaa")
                .header("X-B", "2017-09-01T09:07:46Z");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=aaa b=2017-09-01T09:07:46.000Z");
    }

    @Test
    public void should_return_header_params_with_absent_value() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/params/headers")
                .header("X-A", "aaa");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=aaa b=null");
    }

    @Test
    public void should_return_header_params_with_missing_required_value() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/params/headers");

        assertThat(httpRequest.code()).isEqualTo(500);
    }
}
