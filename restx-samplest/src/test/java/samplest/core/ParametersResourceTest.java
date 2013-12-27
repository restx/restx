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
    public void should_return_query_params() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/params/query/1?a=v1&b=v2");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=v1 b=v2");
    }

    @Test
    public void should_return_query_params_without_optional() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/params/query/1?a=v1");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=v1 b=default");
    }

    @Test
    public void should_return_query_params_datetime() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/params/query/2" +
                        "?a=" + URLEncoder.encode("2013-11-20T14:00:00.000+01:00", "UTF-8") +
                        "&b=" + URLEncoder.encode("2013-12-20T14:00:00.000+01:00", "UTF-8"));
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=2013-11-20T13:00:00.000Z b=2013-12-20T13:00:00.000Z");
    }

    @Test
    public void should_return_query_params_datetime_without_optional() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/params/query/2" +
                        "?a=" + URLEncoder.encode("2013-11-20T14:00:00.000+01:00", "UTF-8"));
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("a=2013-11-20T13:00:00.000Z b=1970-01-01T00:00:00.000Z");
    }

}
