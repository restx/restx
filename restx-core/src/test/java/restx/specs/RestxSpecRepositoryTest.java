package restx.specs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import restx.StdRequest;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 4/8/13
 * Time: 2:05 PM
 */
public class RestxSpecRepositoryTest {
    @Test
    public void should_find_specs_in_classpath() throws Exception {
        RestxSpecRepository resource = new RestxSpecRepository();

        ImmutableMap<String,RestxSpec> allSpecs = resource.findAllSpecs();

        assertThat(allSpecs).isNotEmpty().containsKey("cases/test/test.spec.yaml");
        assertThat(allSpecs.get("cases/test/test.spec.yaml").getTitle()).isEqualTo("should say hello");
    }

    @Test
    public void should_find_specs_for_operation() throws Exception {
        RestxSpecRepository resource = new RestxSpecRepository();

        ImmutableMap<String, RestxSpec> allSpecs = ImmutableMap.of(
                "cases/test/test.spec.yaml", spec("should say hello on path", when("GET", "/messages/xavier")),
                "cases/test/test2.spec.yaml", spec("should say hello w/ query param", when("GET", "/messages?who=xavier")));

        assertThat(resource.filterSpecsByOperation(allSpecs, "GET", "/messages/{id}"))
                .isNotEmpty().containsExactly("cases/test/test.spec.yaml");
        assertThat(resource.filterSpecsByOperation(allSpecs, "GET", "/messages"))
                .isNotEmpty().containsExactly("cases/test/test2.spec.yaml");
    }

    @Test
    public void should_find_specs_for_request() throws Exception {
        RestxSpecRepository resource = new RestxSpecRepository();

        WhenHttpRequest when1;
        WhenHttpRequest when2;
        WhenHttpRequest when3;
        ImmutableMap<String, RestxSpec> allSpecs = ImmutableMap.of(
                "cases/test/test.spec.yaml", spec("should say hello on path",
                            when1 = when("GET", "/messages/xavier")),
                "cases/test/test2.spec.yaml", spec("should say hello w/ query param",
                            when2 = when("GET", "/messages?who=xavier")),
                "cases/test/test3.spec.yaml", spec("should say hello w/ other query param",
                            when3 = when("GET", "/messages?who=jules"))
        );

        assertThat(resource.findWhensMatchingRequest(allSpecs, request("GET", "/messages/xavier")))
                .isNotEmpty().containsExactly(when1);
        assertThat(resource.findWhensMatchingRequest(allSpecs, request("GET", "/messages?who=xavier")))
                .isNotEmpty().containsExactly(when2);
        assertThat(resource.findWhensMatchingRequest(allSpecs, request("GET", "/messages?who=xavier&test=anything")))
                .isNotEmpty().containsExactly(when2);
        assertThat(resource.findWhensMatchingRequest(allSpecs, request("GET", "/messages?who=jules")))
                .isNotEmpty().containsExactly(when3);
    }

    private StdRequest request(String httpMethod, String fullPath) {
        return StdRequest.builder().setBaseUri("http://restx.io").setHttpMethod(httpMethod).setFullPath(fullPath).build();
    }

    private RestxSpec spec(String title, WhenHttpRequest request) {
        return new RestxSpec(title, ImmutableList.<Given>of(), ImmutableList.<When>of(request));
    }

    private WhenHttpRequest when(String method, String path) {
        return new WhenHttpRequest(method, path, ImmutableMap.<String,String>of(), "",
                new ThenHttpResponse(200, ""));
    }

}
