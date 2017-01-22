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
public class CoreResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @After
    public void teardown() {
        Factory.LocalMachines.threadLocal().clear();
    }

    @Test
    public void should_return_hello() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/core/hello?who=restx");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("hello restx");
    }

    @Test
    public void should_return_hello_msg() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/core/hellomsg?who=restx");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\n  \"msg\" : \"hello restx\"\n}");
    }

    @Test
    public void should_return_hello_msg_when_delete_with_param() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").DELETE(
                "/api/core/hellomsg?who=restx");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("hello restx");
    }

    @Test
    public void should_post_hello() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").POST(
                "/api/core/hellomsg").send("{\"msg\": \"restx\"}");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\n  \"msg\" : \"hello restx\"\n}");
    }

    @Test
    public void should_lifecycle_hello() throws Exception {
        LifecycleListenerFilter filter = new LifecycleListenerFilter();
        Factory.LocalMachines.overrideComponents().set("LifecycleListenerFilter", filter);
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/core/hello?who=restx");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("hello restx");

        assertLifecycleMethods(filter, "[Optional.absent()]", "[Optional.of(hello restx)]");
    }

    @Test
    public void should_lifecycle_hello_msg() throws Exception {
        LifecycleListenerFilter filter = new LifecycleListenerFilter();
        Factory.LocalMachines.overrideComponents().set("LifecycleListenerFilter", filter);
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/core/hellomsg?who=restx");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\n  \"msg\" : \"hello restx\"\n}");

        assertLifecycleMethods(filter, "[Optional.absent()]", "[Optional.of(Message{msg='hello restx'})]");
    }

    @Test
    public void should_lifecycle_post_hello_msg() throws Exception {
        LifecycleListenerFilter filter = new LifecycleListenerFilter();
        Factory.LocalMachines.overrideComponents().set("LifecycleListenerFilter", filter);
        HttpRequest httpRequest = server.client().authenticatedAs("admin").POST(
                "/api/core/hellomsg").send("{\"msg\": \"restx\"}");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\n  \"msg\" : \"hello restx\"\n}");

        assertLifecycleMethods(filter, "[Optional.of(Message{msg='restx'})]", "[Optional.of(Message{msg='hello restx'})]");
    }


    protected void assertLifecycleMethods(
            LifecycleListenerFilter filter, String expectedInputToString, String expectedOutputToString) {
        assertThat(filter.matched).hasSize(1);
        assertThat(filter.inputs.toString()).isEqualTo(expectedInputToString);
        assertThat(filter.outputs.toString()).isEqualTo(expectedOutputToString);
        assertThat(filter.beforeWriteContentCount).isEqualTo(1);
        // we can't reliably assert filter.afterWriteContentCount calls: they are made in the server after the response
        // is sent to client, so the test (which is a client) can make the assertions before the call
        //        assertThat(filter.afterWriteContentCount).isEqualTo(1);
    }

}
