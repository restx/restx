package hello.web;

import hello.AppServer;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxSpecRule;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 5:27 PM
 */
public class HelloResourceSpecTest {
    @ClassRule
    public static RestxSpecRule rule = new RestxSpecRule(
            AppServer.WEB_INF_LOCATION,
            AppServer.WEB_APP_LOCATION);

    @Test
    public void should_say_hello() throws Exception {
        rule.runTest("cases/hello/should_say_hello.yaml");
    }
}
