package samplest.optional;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.factory.Factory;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalSettingsTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @After
    public void teardown() {
        Factory.LocalMachines.threadLocal().clear();
    }

    @Test
    public void should_not_find_key4() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/settings/key4");
        assertThat(httpRequest.code()).isEqualTo(404);
    }

    @Test
    public void should_find_key5() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/optional/settings/key5");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("key5");
    }

}
