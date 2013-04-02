package restx.swagger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import restx.specs.RestxSpec;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 4/2/13
 * Time: 10:13 PM
 */
public class SpecsResourceTest {
    @Test
    public void should_find_specs_in_classpath() throws Exception {
        SpecsResource resource = new SpecsResource();

        ImmutableMap<String,RestxSpec> allSpecs = resource.findAllSpecs();

        assertThat(allSpecs).isNotEmpty().containsKey("cases/test/test.spec.yaml");
        assertThat(allSpecs.get("cases/test/test.spec.yaml").getTitle()).isEqualTo("should say hello");
    }

    @Test
    public void should_find_specs_for_operation() throws Exception {
        SpecsResource resource = new SpecsResource();

        ImmutableMap<String, RestxSpec> allSpecs = ImmutableMap.of(
                "cases/test/test.spec.yaml", spec("should say hello on path", "GET", "messages/xavier"),
                "cases/test/test2.spec.yaml", spec("should say hello w/ query param", "GET", "messages?who=xavier"));

        assertThat(resource.filterSpecsByOperation(allSpecs, "GET", "/messages/{id}"))
                .isNotEmpty().containsExactly("cases/test/test.spec.yaml");
        assertThat(resource.filterSpecsByOperation(allSpecs, "GET", "/messages"))
                .isNotEmpty().containsExactly("cases/test/test2.spec.yaml");
    }

    private RestxSpec spec(String title, String method, String path) {
        return new RestxSpec(title, ImmutableList.<RestxSpec.Given>of(), ImmutableList.<RestxSpec.When>of(
                new RestxSpec.WhenHttpRequest(method, path, ImmutableMap.<String,String>of(), "",
                        new RestxSpec.ThenHttpResponse(200, ""))));
    }
}
