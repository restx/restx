package samplest.optional;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.factory.Factory;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @After
    public void teardown() {
        Factory.LocalMachines.threadLocal().clear();
    }

    @Test
    public void should_return_content() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/hasContent");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("hello");
    }

    @Test
    public void should_return_not_found() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/isEmpty");
        assertThat(httpRequest.code()).isEqualTo(404);
    }

    @Test
    public void should_return_optional_parameter() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/optionalParam");
        assertThat(httpRequest.code()).isEqualTo(404);
        httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/optionalParam?param=hello");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("hello");
    }

}