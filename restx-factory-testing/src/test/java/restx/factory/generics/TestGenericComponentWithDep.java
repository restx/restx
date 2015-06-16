package restx.factory.generics;

import javax.inject.Named;
import restx.factory.Component;

/**
 * @author apeyrard
 */
@Component
public class TestGenericComponentWithDep {
	private final TestGenericInterface<Double> testGenericComponent;
	private final String fixedValue;

	public TestGenericComponentWithDep(TestGenericInterface<Double> testGenericComponent, @Named("withNamedDependency") String fixedValue) {
		this.testGenericComponent = testGenericComponent;
		this.fixedValue = fixedValue;
	}

	public String execute(Double element) {
		return testGenericComponent.execute(element) + "#" + fixedValue;
	}
}
