package restx.factory.alternative.components;

import restx.factory.Alternative;
import restx.factory.When;

/**
 * Create an alternative for a component, without knowing its implementation, just the interface.
 *
 * @author apeyrard
 */
@Alternative(to = TestComponentInterface.class, named = "restx.test.component.name")
@When(name = "restx.test.alternatives", value = "true")
public class TestComponentFromInterfaceAlternative implements TestComponentInterface {

	@Override
	public String name() {
		return "alternative";
	}
}
