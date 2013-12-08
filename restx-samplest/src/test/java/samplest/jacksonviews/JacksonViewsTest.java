package samplest.jacksonviews;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.server.WebServers;
import restx.server.simple.simple.SimpleWebServer;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.tests.HttpTestClient.GET;

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
        HttpRequest httpRequest = GET(server.getServer().baseUrl() + "/api/jacksonviews/carsDetails");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(
                "[" +
                    "{\"brand\":\"Brand1\",\"model\":\"Model1\"," +
                        "\"status\":{\"status\":\"ko\",\"details\":\"\"}," +
                        "\"details\":\"Detail1\"}," +
                    "{\"brand\":\"Brand1\",\"model\":\"Model2\"," +
                        "\"status\":{\"status\":\"ok\",\"details\":\"status detail 2\"}," +
                        "\"details\":\"Detail2\"}" +
                "]");
    }
    @Test
    public void should_retrieve_default_view() throws Exception {
        HttpRequest httpRequest = GET(server.getServer().baseUrl() + "/api/jacksonviews/cars");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo(
                "[" +
                    "{\"brand\":\"Brand1\",\"model\":\"Model1\"," +
                        "\"status\":{\"status\":\"ko\"}}," +
                    "{\"brand\":\"Brand1\",\"model\":\"Model2\"," +
                        "\"status\":{\"status\":\"ok\"}}" +
                "]");
    }
}
