package restx.factory.generics;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import javax.inject.Named;
import restx.factory.Module;
import restx.factory.Provides;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Module
public class TestModuleWithGenerics {

	@Provides
	public TestGenericInterface<Integer> integerTestGenericInterface() {
		return new TestGenericInterface<Integer>() {
			@Override
			public String execute(Integer element) {
				return String.valueOf(element);
			}
		};
	}

	@Provides
	 public TestGenericInterface<Long> longTestGenericInterface() {
		return new TestGenericInterface<Long>() {
			@Override
			public String execute(Long element) {
				return String.valueOf(element);
			}
		};
	}

	@Provides
	 public TestGenericInterface<TestGenericInterface<TestGenericInterface<Long>>> longTestGenericInterface2() {
		return new TestGenericInterface<TestGenericInterface<TestGenericInterface<Long>>>() {
			@Override
			public String execute(TestGenericInterface<TestGenericInterface<Long>> element) {
				return "foo";
			}
		};
	}

	@Provides
	public Map<String, Double> longTestGenericInterfaceMap() {
		return ImmutableMap.of("foo", 23d);
	}

	@Provides(priority = -1000)
	@When(name = "answer-of", value = "universe")
	public Map<String, Long> longTestGenericInterfaceMapConditional() {
		return ImmutableMap.of("foo", 42l);
	}

	@Provides
	public String withDependency(TestGenericInterface<Long> dep) {
		return dep.execute(42l);
	}

	@Provides
	public String withNamedDependency(@Named("longTestGenericInterface") TestGenericInterface<Long> dep) {
		return dep.execute(23l);
	}
}
