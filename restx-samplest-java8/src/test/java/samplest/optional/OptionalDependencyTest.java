package samplest.optional;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.factory.Factory;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalDependencyTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @After
    public void teardown() {
        Factory.LocalMachines.threadLocal().clear();
    }

    @Test
    public void should_not_find_foo() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/dependency/foo");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("false");
    }

    @Test
    public void should_find_bar() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/dependency/bar");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("true");
    }

}