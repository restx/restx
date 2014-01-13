package samplest.jacksonviews;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: eoriou
 * Date: 04/12/2013
 * Time: 15:52
 */
public class JacksonViewsTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_retrieve_details_view() throws Exception {
        HttpRequest httpRequest = server.client().GET("/api/jacksonviews/carsDetails");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(
                "[ {\n" +
                        "  \"brand\" : \"Brand1\",\n" +
                        "  \"model\" : \"samplest-Model1\",\n" +
                        "  \"status\" : {\n" +
                        "    \"status\" : \"ko\",\n" +
                        "    \"details\" : \"\"\n" +
                        "  },\n" +
                        "  \"details\" : \"Detail1\"\n" +
                        "}, {\n" +
                        "  \"brand\" : \"Brand1\",\n" +
                        "  \"model\" : \"samplest-Model2\",\n" +
                        "  \"status\" : {\n" +
                        "    \"status\" : \"ok\",\n" +
                        "    \"details\" : \"status detail 2\"\n" +
                        "  },\n" +
                        "  \"details\" : \"Detail2\"\n" +
                        "} ]");
    }
    @Test
    public void should_retrieve_default_view() throws Exception {
        HttpRequest httpRequest = server.client().GET("/api/jacksonviews/cars");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(
                "[ {\n" +
                        "  \"brand\" : \"Brand1\",\n" +
                        "  \"model\" : \"samplest-Model1\",\n" +
                        "  \"status\" : {\n" +
                        "    \"status\" : \"ko\"\n" +
                        "  }\n" +
                        "}, {\n" +
                        "  \"brand\" : \"Brand1\",\n" +
                        "  \"model\" : \"samplest-Model2\",\n" +
                        "  \"status\" : {\n" +
                        "    \"status\" : \"ok\"\n" +
                        "  }\n" +
                        "} ]");
    }
}
