package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see samplest.core.ProvidesWithExceptionResource
 */
public class ProvidesWithExceptionsTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_access_resource_relying_on_module_declaring_exceptions() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/providesWithExceptions");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("noproblem");
    }
}
