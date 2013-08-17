package restx.servers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.factory.NamedComponent;
import restx.server.WebServerSupplier;
import restx.tests.RestxSpecRule;
import restx.tests.RestxSpecRunner;

/**
 * @author fcamblor
 */
@RunWith(Parameterized.class)
public class SessionsTest {
    @Rule
    public RestxSpecRule rule;

    private String name;
    private WebServerSupplier webServerSupplier;

    @Parameterized.Parameters(name="{0}") // Parameter name will be the component's name
    public static Iterable<Object[]> data(){
        // Fetching every webserversuppliers provided in classpath
        return Collections2.transform(
            RestxSpecRunner.defaultFactory().queryByClass(WebServerSupplier.class).find(),
            new Function<NamedComponent<WebServerSupplier>, Object[]>() {
                @Override
                public Object[] apply(NamedComponent<WebServerSupplier> input) {
                    return new Object[]{ input.getName().getName(), input.getComponent() };
                }
        });
    }

    public SessionsTest(String name, WebServerSupplier webServerSupplier){
        this.name = name;
        this.webServerSupplier = webServerSupplier;
        this.rule = new RestxSpecRule(this.webServerSupplier);
    }


    @Test
    public void should_authentication_be_successful() throws Exception {
        rule.runTest("specs/sessions/should_authentication_be_successful.spec.yaml");
    }

    @Test
    public void should_authentication_be_in_failure() throws Exception {
        rule.runTest("specs/sessions/should_authentication_be_in_failure.spec.yaml");
    }

    @Test
    public void should_disconnection_be_successful() throws Exception {
        rule.runTest("specs/sessions/should_disconnection_be_successful.spec.yaml");
    }
}