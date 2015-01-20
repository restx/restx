package restx.factory.alternative.components;

import restx.factory.Alternative;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Alternative(to = TestComponentNamed.class)
@When(name = "restx.test.alternatives", value = "true")
public class TestComponentNamedAlternative extends TestComponentNamed {

	@Override
	public String speed() {
		return "fast";
	}
}
