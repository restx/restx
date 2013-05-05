package restx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 4/2/13
 * Time: 9:39 PM
 */
public class StdRequestTest {
    @Test
    public void should_build_restx_uri() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api").setRestxPath("/message")
                .setQueryParams(ImmutableMap.<String, ImmutableList<String>>of("q", ImmutableList.of("val1", "val2"))).build();
        assertThat(request.getBaseUri()).isEqualTo("http://localhost:8080/api");
        assertThat(request.getRestxPath()).isEqualTo("/message");
        assertThat(request.getRestxUri()).isEqualTo("/message?q=val1&q=val2");
    }

    @Test
    public void should_build_restx_uri_from_full_path() throws Exception {
        StdRequest request = StdRequest.builder().setBaseUri("http://localhost:8080/api")
                .setFullPath("/message?q=val1&q=val2")
                .build();
        assertThat(request.getBaseUri()).isEqualTo("http://localhost:8080/api");
        assertThat(request.getRestxPath()).isEqualTo("/message");
        assertThat(request.getRestxUri()).isEqualTo("/message?q=val1&q=val2");
    }
}
