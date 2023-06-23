package restx;

import com.github.kevinsawicki.http.HttpRequest;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import java.io.IOException;

import static restx.server.Jetty11WebServer.jettyWebServerSupplier;


/**
 * @author fcamblor
 */
public class ResourcesRouteTest {

    @Rule
    public RestxServerRule restxServer = new RestxServerRule(
            jettyWebServerSupplier("src/test/webapp/WEB-INF/web.xml", "src/test/webapp") );

    @Test
    public void should_static_content_be_served_from_classpath() throws IOException {
        HttpRequest httpRequest = HttpRequest.get(restxServer.getServer().baseUrl() + "/web/hello.txt");

        Assertions.assertThat(httpRequest.code()).isEqualTo(200);
        Assertions.assertThat(httpRequest.body().trim()).isEqualTo("Hello world !");
    }
}
