package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import org.joda.time.DateTime;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxServerRule;

import static org.assertj.core.api.Assertions.assertThat;

public class HeadersResourceTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();

    @Test
    public void should_return_expire_header() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin").GET(
                "/api/headers/expires");
        assertThat(httpRequest.code()).isEqualTo(200);

        long duration = Math.abs(httpRequest.expires() - DateTime.now().plusDays(2).plusHours(4).getMillis());
        assertThat(duration).isLessThan(5000);
    }
}
