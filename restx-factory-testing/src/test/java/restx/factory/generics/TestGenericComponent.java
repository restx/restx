package restx.factory.generics;

import restx.factory.Component;

/**
 * @author apeyrard
 */
@Component
public class TestGenericComponent implements TestGenericInterface<Double> {

	@Override
	public String execute(Double element) {
		return String.valueOf(element);
	}
}
