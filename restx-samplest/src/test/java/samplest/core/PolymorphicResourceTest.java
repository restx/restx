package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.HttpTestClient;
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
        assertThat(httpRequest.body().trim()).isEqualTo(String.format("{%n" +
                "  \"@class\" : \".PolymorphicResource$A\",%n" +
                "  \"a\" : \"a\"%n" +
                "}"));
    }

    @Test
    public void should_return_B() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/polymorphic/single/B");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(String.format("{%n" +
                "  \"@class\" : \".PolymorphicResource$B\",%n" +
                "  \"a\" : \"a\",%n" +
                "  \"b\" : \"b\"%n" +
                "}"));
    }

    @Test
    public void should_return_B_list() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/polymorphic/list/B");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(String.format("[ {%n" +
                "  \"@class\" : \".PolymorphicResource$A\",%n" +
                "  \"a\" : \"a1\"%n" +
                "}, {%n" +
                "  \"@class\" : \".PolymorphicResource$B\",%n" +
                "  \"a\" : \"a2\",%n" +
                "  \"b\" : \"b\"%n" +
                "} ]"));
    }

    @Test
    public void should_post_A() throws Exception {
        HttpTestClient httpTestClient = HttpTestClient.withBaseUrl("http://localhost:8080");
        httpTestClient = server.client();
        HttpRequest httpRequest = httpTestClient.authenticatedAs("admin")
                .POST("/api/polymorphic")
                .contentType("application/json")
                .send("{\"@class\":\".PolymorphicResource$A\",\"a\":\"a3\"}");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(String.format("{%n" +
                "  \"@class\" : \".PolymorphicResource$A\",%n" +
                "  \"a\" : \"a3\"%n" +
                "}"));
    }

    @Test
    public void should_post_B() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .POST("/api/polymorphic")
                .contentType("application/json")
                .send("{\"@class\":\".PolymorphicResource$B\",\"a\":\"a3\",\"b\":\"b\"}");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(String.format("{%n" +
                "  \"@class\" : \".PolymorphicResource$B\",%n" +
                "  \"a\" : \"a3\",%n" +
                "  \"b\" : \"b\"%n" +
                "}"));
    }
}
