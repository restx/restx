package restx.factory.conditional.components;

/**
 * @author apeyrard
 */

import java.util.Comparator;
import javax.inject.Named;
import restx.factory.Component;
import restx.factory.When;

@Component(asClass = Comparator.class)
@Named("test.comparator")
@When(name = "allow.comparator", value = "true")
public class TestConditionalAsClass implements Comparator {
	@Override
	public int compare(Object o1, Object o2) {
		return 0;
	}
}
