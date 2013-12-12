package samplest.security;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.HttpTestClient;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.tests.HttpTestClient.withBaseUrl;

/**
 * Date: 12/12/13
 * Time: 19:12
 */
public class SecuredResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_access_secured_resource() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/security/user");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("admin");
    }
}
