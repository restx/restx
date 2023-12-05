package samplest.hello;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeNullableKotlinResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_return_integer() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/int-number-kt");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("1");
    }

    @Test
    public void should_return_long() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/long-number-kt");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("1");
    }

    @Test
    public void should_return_double() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/double-number-kt");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("1.0");
    }

    @Test
    public void should_return_float() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/float-number-kt");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("1.0");
    }

    @Test
    public void should_return_byte() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/byte-number-kt");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("1");
    }

    @Test
    public void should_return_boolean() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/boolean-kt");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isEqualTo("true");
    }

    @Test
    public void should_return_404_when_string_is_kotlin_nullable() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/string-nullable-kt");

        assertThat(httpRequest.code()).isEqualTo(404);
    }

    @Test
    public void should_return_string() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/string-non-null-kt");

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("coucou");
    }

    @Test
    public void should_accept_kotlin_nullable_in_queryParams() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/find-query-nullable-kotlin?nonNull=test");

        assertThat(httpRequest.code()).isEqualTo(404);
    }

    @Test
    public void should_accept_kotlin_nullable_in_criteria() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/find-nullable-criteria-kotlin");

        assertThat(httpRequest.code()).isEqualTo(404);
    }

    @Test
    public void should_not_accept_kotlin_nullable_in_criteria() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/find-criteria-kotlin");

        assertThat(httpRequest.code()).isEqualTo(500);
        assertThat(httpRequest.body()).contains("QUERY param <criteria> is required");
    }

    @Test
    public void should_accept_kotlin_criteria() {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/find-criteria-kotlin?nonNull=test");

        assertThat(httpRequest.code()).isEqualTo(200);

        final String body = httpRequest.body().trim().replace("\n", "").replace(" ", "");
        assertThat(body).contains("{\"nonNull\":\"test\"}");
    }
}
