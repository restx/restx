package restx.factory.generics;

import restx.factory.Alternative;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Alternative(to = TestGenericComponent.class)
@When(name = "answer-of", value = "universe")
public class TestGenericComponentAlternative extends TestGenericComponent {

	@Override
	public String execute(Double element) {
		return String.valueOf(42d);
	}
}
