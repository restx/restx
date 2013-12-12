package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.factory.Factory.LocalMachines.overrideComponents;
import static restx.factory.Factory.LocalMachines.threadLocal;

/**
 * @see ClientAffinityResource
 */
public class ClientAffinityTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @After
    public void cleanup() {
        threadLocal().clear();
    }

    @Test
    public void should_share_threadlocal_components_with_server() throws Exception {
        // first we try the default implementation of the ClientAffinityResource
        HttpRequest httpRequest = server.client().GET("/api/clientAffinity");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("NONE");

        // now we provide a component in thread local, which should be used on the server for this test
        overrideComponents().set(ClientAffinityResource.COMPONENT_NAME, "myvalue");
        httpRequest = server.client().GET("/api/clientAffinity");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("myvalue");
    }
}
