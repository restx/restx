package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.factory.Factory;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 13/12/13
 * Time: 23:09
 */
public class WebExceptionResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @After
    public void teardown() {
        Factory.LocalMachines.threadLocal().clear();
    }

    @Test
    public void should_send_redirect() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/core/webexception/redirect").followRedirects(false);
        assertThat(httpRequest.code()).isEqualTo(302);
        assertThat(httpRequest.header("Location")).isEqualTo("/api/core/hello?who=restx");
    }

    @Test
    public void should_follow_redirect() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/core/webexception/redirect");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("hello restx");
    }
}
