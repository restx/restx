package samplest.settings;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.tests.HttpTestClient.GET;

/**
 * @see SettingsResource
 */
public class SettingsTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_access_keys() throws Exception {
        HttpRequest httpRequest = GET(
                server.getServer().baseUrl() + "/api/settings/key1");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("value1");

        httpRequest = GET(
                server.getServer().baseUrl() + "/api/settings/key2");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("value2");

        httpRequest = GET(
                server.getServer().baseUrl() + "/api/settings/key3");
        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body().trim()).isEqualTo("MyValue3");
    }

}
