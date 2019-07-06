package samplest.security;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.hash.Hashing;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.security.HttpAuthenticationFilter;
import restx.security.RestxSessionBareFilter;
import restx.security.RestxSessionCookieDescriptor;
import restx.security.RestxSessionCookieFilter;
import restx.tests.HttpTestClient;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static restx.factory.Factory.LocalMachines.threadLocal;
import static restx.factory.Factory.activationKey;

/**
 * Date: 12/12/13
 * Time: 19:12
 */
public class SecuredResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @After
    public void teardown() {
        threadLocal().clear();
    }

    @Test
    public void should_access_secured_resource() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/security/user");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("admin");
    }

    @Test
    public void should_access_secured_resource_with_su() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET("/api/security/user")
                .header("RestxSu", "{ \"principal\": \"user1\" }");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("user1");
    }

    @Test
    public void should_access_secured_resource_with_http_basic() throws Exception {
        HttpTestClient client = server.client();
        HttpRequest httpRequest = client.GET("/api/security/user")
                .basic("admin", Hashing.md5().hashString("juma", Charsets.UTF_8).toString());

        Optional<NamedComponent<RestxSessionCookieDescriptor>> restxSessionCookieDescriptorNamedComponentOptional =
                Factory.getInstance().queryByClass(RestxSessionCookieDescriptor.class).findOne();

        if(!restxSessionCookieDescriptorNamedComponentOptional.isPresent()) {
            throw new IllegalStateException();
        }

        String headerPrincipalDecode = decodeRestxSession(httpRequest, restxSessionCookieDescriptorNamedComponentOptional);

        assertThat(httpRequest.code()).isEqualTo(200);
        assertResponseSetCookieContainsKeyAndValue(headerPrincipalDecode, "principal", "admin");
        assertThat(httpRequest.body().trim()).isEqualTo("admin");
    }

    private String decodeRestxSession(final HttpRequest httpRequest, final Optional<NamedComponent<RestxSessionCookieDescriptor>> restxSessionCookieDescriptorNamedComponentOptional) {
        RestxSessionCookieDescriptor restxSessionCookieDescriptor = restxSessionCookieDescriptorNamedComponentOptional
                .get()
                .getComponent();
        String headerPrincipal = httpRequest.headers("Set-Cookie")[1];
        String headerPrincipalValue = headerPrincipal.substring(headerPrincipal.indexOf("RestxSession=") + 13, headerPrincipal.lastIndexOf(";Path"));
        return restxSessionCookieDescriptor.decodeValueIfNeeded(headerPrincipalValue);
    }

    private void assertResponseSetCookieContainsKeyAndValue(String responseSetCookieHeader, String key, String value) {
        org.hamcrest.MatcherAssert.assertThat(responseSetCookieHeader, anyOf(
                // Depending on restx-server implementation, Set-Cookie may surround its cookie value with double quotes,
                // thus surrounding key/value with backslash escapes
                containsString(String.format("\"%s\":\"%s\"", key, value)),
                containsString(String.format("\\\"%s\\\":\\\"%s\\\"", key, value))
        ));
    }

    @Test
    public void should_not_access_secured_resource_with_http_basic_when_deactivated() throws Exception {
        threadLocal().set(activationKey(HttpAuthenticationFilter.class, "HttpAuthenticationFilter"), "false");
        HttpRequest httpRequest = server.client().GET("/api/security/user")
                .basic("admin", Hashing.md5().hashString("juma", Charsets.UTF_8).toString());
        assertThat(httpRequest.code()).isEqualTo(401);
    }

    @Test
    public void should_access_secured_resource_with_http_basic_no_session() throws Exception {
        threadLocal()
                .set(activationKey(RestxSessionCookieFilter.class, "RestxSessionCookieFilter"), "false")
                .set(activationKey(RestxSessionBareFilter.class, "RestxSessionBareFilter"), "true");
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
