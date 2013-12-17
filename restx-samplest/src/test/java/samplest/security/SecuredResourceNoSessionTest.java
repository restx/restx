package samplest.security;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.security.HttpAuthenticationFilter;
import restx.security.RestxSessionBareFilter;
import restx.security.RestxSessionFilter;
import restx.tests.HttpTestClient;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.factory.Factory.LocalMachines.contextLocal;
import static restx.factory.Factory.LocalMachines.threadLocal;
import static restx.factory.Factory.activationKey;

/**
 * Date: 12/12/13
 * Time: 19:12
 */
public class SecuredResourceNoSessionTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule() {
        @Override
        protected void afterServerCreated() {
            contextLocal(getServer().getServerId())
                    .set(activationKey(RestxSessionFilter.class, "RestxSessionFilter"), "false")
                    // activating a component which is deactivated by default can be done in thread local ATM
                    .set(activationKey(RestxSessionBareFilter.class, "RestxSessionBareFilter"), "true");
        }
    };

    @After
    public void teardown() {
        threadLocal().clear();
    }

    @Test
    public void should_access_secured_resource_with_http_basic_no_session() throws Exception {
        HttpTestClient client = server.client();
        HttpRequest httpRequest = client.GET("/api/security/user")
                .basic("admin", Hashing.md5().hashString("juma", Charsets.UTF_8).toString());
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.header("Set-Cookie")).isNull();
        assertThat(httpRequest.body().trim()).isEqualTo("admin");

        httpRequest = client.GET("/api/security/user");
        assertThat(httpRequest.code()).isEqualTo(401);
    }
}
