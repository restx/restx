package restx.servers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.factory.NamedComponent;
import restx.server.WebServerSupplier;
import restx.specs.RestxSpec;
import restx.tests.RestxSpecRule;
import restx.tests.RestxSpecRunner;
import restx.tests.RestxSpecTests;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author fcamblor
 */
@RunWith(Parameterized.class)
public class SessionsTest {
    @Rule
    public RestxSpecRule rule;

    private final RestxSpec spec;
    private final WebServerSupplier webServerSupplier;

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data() throws IOException {
        Set<NamedComponent<WebServerSupplier>> webServerSuppliers = RestxSpecRunner.defaultFactory().queryByClass(WebServerSupplier.class).find();
        List<RestxSpec> specs = RestxSpecTests.findSpecsIn("specs/sessions");

        List<Object[]> data = newArrayList();
        for(NamedComponent<WebServerSupplier> webServerSupplierNamedComponent : webServerSuppliers){
            for(RestxSpec restxSpec: specs){
                data.add(new Object[]{
                        String.format("spec [%s] with server %s",
                                restxSpec.getTitle(),
                                webServerSupplierNamedComponent.getName().getName()),
                        restxSpec,
                        webServerSupplierNamedComponent.getComponent() });
            }
        }
        return data;
    }

    // name param is only used for the @Parameters' name attribute
    public SessionsTest(String name, RestxSpec spec, WebServerSupplier webServerSupplier){
        this.webServerSupplier = webServerSupplier;
        this.spec = spec;
        this.rule = new RestxSpecRule(this.webServerSupplier);
    }


    @Test
    public void should_server_scenario_be_ok() throws Exception {
        this.rule.runTest(this.spec);
    }
}