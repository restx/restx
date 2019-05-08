package restx.i18n;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 2/2/14
 * Time: 08:45
 */
public class MessagesRouterTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_get_labels_json() throws Exception {
        HttpRequest request = server.client().GET("/api/i18n/labels.json");

        assertThat(request.code()).isEqualTo(200);
        assertThat(request.body()).isEqualTo(String.format("{%n" +
                "  \"key1\" : \"value1\",\n" +
                "  \"key2\" : \"hello {{test}}\"\n" +
                "}%n"));
    }
    
    @Test
    public void should_get_labels_js() throws Exception {
        HttpRequest request = server.client().GET("/api/i18n/labels.js");

        assertThat(request.code()).isEqualTo(200);
        assertThat(request.body()).isEqualTo(String.format(
                "// RESTX Labels - customize this with restx.i18n.labelsJsTemplate named String\n" +
                "window.labels = {%n" +
                "  \"key1\" : \"value1\",\n" +
                "  \"key2\" : \"hello {{test}}\"\n" +
                "};%n"));
    }
}
