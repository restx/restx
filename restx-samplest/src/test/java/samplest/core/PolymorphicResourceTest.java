package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 13/12/13
 * Time: 23:09
 */
public class PolymorphicResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_return_A() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/polymorphic/single/A");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\"@class\":\".PolymorphicResource$A\",\"a\":\"a\"}");
    }

    @Test
    public void should_return_B() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/polymorphic/single/B");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\"@class\":\".PolymorphicResource$B\",\"a\":\"a\",\"b\":\"b\"}");
    }

    @Test
    public void should_return_B_list() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/polymorphic/list/B");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("[{\"@class\":\".PolymorphicResource$A\",\"a\":\"a1\"},{\"@class\":\".PolymorphicResource$B\",\"a\":\"a2\",\"b\":\"b\"}]");
    }

    @Test
    public void should_post_A() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").POST(
                "/api/polymorphic").send("{\"@class\":\".PolymorphicResource$A\",\"a\":\"a3\"}");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\"@class\":\".PolymorphicResource$A\",\"a\":\"a3\"}");
    }

    @Test
    public void should_post_B() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").POST(
                "/api/polymorphic").send("{\"@class\":\".PolymorphicResource$B\",\"a\":\"a3\",\"b\":\"b\"}");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("{\"@class\":\".PolymorphicResource$B\",\"a\":\"a3\",\"b\":\"b\"}");
    }
}
