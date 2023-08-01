package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeNullableResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_return_integer() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/int-number");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("1");
    }

    @Test
    public void should_return_long() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/long-number");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("1");
    }

    @Test
    public void should_return_double() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/double-number");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("1.0");
    }

    @Test
    public void should_return_float() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/float-number");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("1.0");
    }

    @Test
    public void should_return_byte() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/byte-number");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("1");
    }

    @Test
    public void should_return_boolean() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/boolean");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("true");
    }
}
