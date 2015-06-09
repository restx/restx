package restx.factory.generics;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.factory.Factory.LocalMachines.overrideComponents;
import static restx.factory.Factory.LocalMachines.threadLocal;


import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import restx.common.TypeReference;
import restx.factory.Factory;
import restx.factory.Name;

/**
 * @author apeyrard
 */
public class GenericsTest {

	@Rule
	public JUnitSoftAssertions softly = new JUnitSoftAssertions();

	/**
	 * ElementsFromConfig component can not be build, because of module TestMandatoryDependency
	 * which use a missing dependency.
	 */
	@BeforeClass
	public static void deactivateElementsFromConfig() {
		System.setProperty("restx.activation::restx.factory.FactoryMachine::ElementsFromConfig", "false");
	}

	/**
	 * cleanup state before each test method
	 */
	@Before
	public void cleanupBefore() {
		threadLocal().clear();
	}

	/**
	 * cleanup state after this test class execution
	 */
	@AfterClass
	public static void cleanupAfterClass() {
		threadLocal().clear();
	}

	@Test
	public void should_query_generics_components() {
		Factory factory = Factory.newInstance();

		TestGenericInterface<Double> componentDouble = factory.getComponent(new TypeReference<TestGenericInterface<Double>>() {});
		softly.assertThat(componentDouble.execute(23d)).isEqualTo("23.0");

		TestGenericInterface<Integer> componentInteger = factory.getComponent(new TypeReference<TestGenericInterface<Integer>>() {});
		softly.assertThat(componentInteger.execute(23)).isEqualTo("23");

		TestGenericInterface<Long> component = factory.getComponent(new TypeReference<TestGenericInterface<Long>>() {});
		softly.assertThat(component.execute(23L)).isEqualTo("23");
	}

	@Test
	public void should_query_by_raw_type_generics_components() {
		Factory factory = Factory.newInstance();

		Set<Name<TestGenericInterface>> names = factory.queryByClass(TestGenericInterface.class).findNames();
		assertThat(names).extracting("type").containsOnly(
				TestGenericComponent.class,
				new TypeReference<TestGenericInterface<Integer>>() {}.getType(),
				new TypeReference<TestGenericInterface<Long>>() {}.getType(),
				new TypeReference<TestGenericInterface<TestGenericInterface<TestGenericInterface<Long>>>>() {}.getType()
		);

		Set<TestGenericInterface> components = factory.getComponents(TestGenericInterface.class);
		softly.assertThat(components).hasSize(4);
	}

	@Test
	public void should_query_type_with_multiple_parameters() {
		Factory factory = Factory.newInstance();

		Map<String, Double> component = factory.getComponent(new TypeReference<Map<String, Double>>() {});
		softly.assertThat(component.get("foo")).isEqualTo(23d);
	}

	@Test
	public void should_query_generics_of_generics() {
		Factory factory = Factory.newInstance();

		TestGenericInterface<TestGenericInterface<TestGenericInterface<Long>>> component =
				factory.getComponent(new TypeReference<TestGenericInterface<TestGenericInterface<TestGenericInterface<Long>>>>() {});
		softly.assertThat(component.execute(null)).isEqualTo("foo");
	}

	@Test
	public void should_query_allow_generics_in_conditional() {
		Factory factory = Factory.newInstance();
		softly.assertThat(factory.queryByType(new TypeReference<Map<String, Long>>() {}).findOne().isPresent()).isFalse();

		overrideComponents().set("answer-of", "universe");

		factory = Factory.newInstance();

		Map<String, Long> component = factory.getComponent(new TypeReference<Map<String, Long>>() {});
		softly.assertThat(component.get("foo")).isEqualTo(42);

		TestGenericInterface<Double> componentDouble = factory.getComponent(new TypeReference<TestGenericInterface<Double>>() {});
		softly.assertThat(componentDouble.execute(23d)).isEqualTo("42.0");
	}
}
