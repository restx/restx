package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.tests.RestxServerRule;

import java.net.URLEncoder;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 13/12/13
 * Time: 23:59
 */
@RunWith(Parameterized.class)
public class UrlParametersResourceTest {
    private final String url;
    private final String expectedBody;

    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {"/v1/hello%20world/35v4/v5", "a=v1 b=hello world c=35 d=v4 e=v5"},
        });
    }

    public UrlParametersResourceTest(String url, String expectedBody) {
        this.url = url;
        this.expectedBody = expectedBody;
    }

    @Test
    public void should_return_path_params() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/params/path"+this.url);
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(this.expectedBody);
    }
}
