package restx.factory.alternative.components;

import javax.inject.Named;
import restx.factory.Component;

/**
 * Simple component, without {@link Named} annotation, nor interface implementation.
 *
 * @author apeyrard
 */
@Component
public class TestComponentSimple {

	public String greet() {
		return "hello";
	}
}
