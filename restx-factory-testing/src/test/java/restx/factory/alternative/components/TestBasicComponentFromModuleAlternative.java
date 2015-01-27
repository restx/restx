package restx.factory.alternative.components;

import restx.factory.Alternative;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Alternative(to = TestComponentsFromModule.SomeInterface.class, named = "production")
@When(name = "restx.test.alternatives", value = "true")
public class TestBasicComponentFromModuleAlternative implements TestComponentsFromModule.SomeInterface {

	@Override
	public String mode() {
		return "dev";
	}
}
