package restx.factory.alternative.components;

import restx.factory.Alternative;
import restx.factory.When;

/**
 * Create an alternative for component {@link TestComponentSimple}.
 *
 * @author apeyrard
 */
@Alternative(to = TestComponentSimple.class)
@When(name = "restx.test.alternatives", value = "true")
public class TestComponentSimpleAlternative extends TestComponentSimple {

	@Override
	public String greet() {
		return "bonjour";
	}
}
