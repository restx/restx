package restx.factory.alternative.components;

import javax.inject.Named;
import restx.factory.Component;

/**
 * Component based on an interface
 *
 * @author apeyrard
 */
@Component
@Named("restx.test.component.name")
public class TestComponentFromInterface implements TestComponentInterface {

	@Override
	public String name() {
		return "original";
	}
}
