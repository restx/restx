package restx.server.simple;

import org.junit.ClassRule;
import org.junit.Test;
import restx.server.WebServer;
import restx.server.WebServerSupplier;
import restx.server.simple.simple.SimpleWebServer;
import restx.tests.RestxSpecRule;

/**
 * @author fcamblor
 */
public class SessionsTest {
    @ClassRule
    public static RestxSpecRule rule = new RestxSpecRule("/api", new WebServerSupplier() {
        @Override
        public WebServer newWebServer(int port) {
            return SimpleWebServer.builder().setPort(port).build();
        }
    }, RestxSpecRule.defaultFactory());

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