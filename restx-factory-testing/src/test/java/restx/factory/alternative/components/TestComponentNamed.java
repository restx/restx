package restx.factory.alternative.components;

import javax.inject.Named;
import restx.factory.Component;

/**
 * A component using a {@link Named} annotation.
 *
 * @author apeyrard
 */
@Component
@Named("restx.test.component.speed")
public class TestComponentNamed {

	public String speed() {
		return "slow";
	}
}
