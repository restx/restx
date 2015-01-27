package restx.factory.alternative.components;

import restx.factory.Alternative;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Alternative(to = TestComponentsFromModule.SomeOtherInterface.class, named = "restx.test.component.productionNamed")
@When(name = "restx.test.alternatives", value = "true")
public class TestNamedComponentFromModuleAlternative implements TestComponentsFromModule.SomeOtherInterface {

	@Override
	public String mode() {
		return "dev";
	}
}
