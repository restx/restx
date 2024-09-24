package restx.jongo;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import restx.jongo.specs.tests.MongoRestxSpecTestsRunner;
import restx.tests.FindSpecsIn;
import restx.tests.RestxServerRule;
import restx.tests.RestxSpecTestsRunner;

import static restx.server.JettyWebServer.jettyWebServerSupplier;

/**
 * User: Christophe Labouisse
 * Date: 11/06/2014
 * Time: 15:33
 */
@RunWith(RestxSpecTestsRunner.class)
@FindSpecsIn("restx/jongo")
public class UserCredentialsTest {
}
