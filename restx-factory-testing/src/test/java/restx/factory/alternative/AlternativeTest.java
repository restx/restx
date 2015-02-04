package restx.factory.alternative;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.factory.Factory.LocalMachines.overrideComponents;
import static restx.factory.Factory.LocalMachines.threadLocal;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.alternative.components.TestAlternativesFromModule;
import restx.factory.alternative.components.TestComponentInterface;
import restx.factory.alternative.components.TestComponentNamed;
import restx.factory.alternative.components.TestComponentSimple;
import restx.factory.alternative.components.TestComponentsFromModule;

/**
 * Test cases for alternatives.
 *
 * @author apeyrard
 */
public class AlternativeTest {

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

	/*
		This test uses the TestComponentSimple, and TestComponentSimpleAlternative. It uses the default alternative
		mechanism without using Named annotation.
	 */
	@Test
	public void should_use_alternative_for_basic_components() {
		Factory factory = Factory.newInstance();
		TestComponentSimple component = factory.getComponent(TestComponentSimple.class);
		assertThat(component.greet()).isEqualTo("hello");

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(TestComponentSimple.class);
		assertThat(component.greet()).isEqualTo("bonjour");
	}

	/*
		This test uses the TestComponentNamed and TestComponentNamedAlternative, the alternative
		should be registered under the name defined in the reference component
	 */
	@Test
	public void should_use_alternative_for_named_components() {
		Factory factory = Factory.newInstance();
		TestComponentNamed component = factory.getComponent(Name.of(TestComponentNamed.class, "restx.test.component.speed"));
		assertThat(component.speed()).isEqualTo("slow");

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(Name.of(TestComponentNamed.class, "restx.test.component.speed"));
		assertThat(component.speed()).isEqualTo("fast");
	}

	/*
		This test uses component based on an interface, the alternative reference the interface and force to be registered
		 with the same name	as the original component.
	 */
	@Test
	public void should_use_alternative_referencing_an_interface() {
		Factory factory = Factory.newInstance();
		TestComponentInterface component = factory.getComponent(Name.of(TestComponentInterface.class, "restx.test.component.name"));
		assertThat(component.name()).isEqualTo("original");

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(Name.of(TestComponentInterface.class, "restx.test.component.name"));
		assertThat(component.name()).isEqualTo("alternative");
	}

	/*
		This test uses a component provided by a module, in order to create an alternative, it has to use the name of the
		method annotated with @Provides.
	 */
	@Test
	public void should_use_alternative_for_provided_component_using_the_method_as_name() {
		Factory factory = Factory.newInstance();
		TestComponentsFromModule.SomeInterface component = factory.getComponent(TestComponentsFromModule.SomeInterface.class);
		assertThat(component.mode()).isEqualTo("production");

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(TestComponentsFromModule.SomeInterface.class);
		assertThat(component.mode()).isEqualTo("dev");
	}

	/*
		This test uses a named component provided by a module, in order to create an alternative, it has to use the same
		name as the component.
	 */
	@Test
	public void should_use_alternative_for_provided_named_component_using_same_name() {
		Factory factory = Factory.newInstance();
		TestComponentsFromModule.SomeOtherInterface component = factory.getComponent(
				Name.of(TestComponentsFromModule.SomeOtherInterface.class, "restx.test.component.productionNamed"));
		assertThat(component.mode()).isEqualTo("production");

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(Name.of(TestComponentsFromModule.SomeOtherInterface.class, "restx.test.component.productionNamed"));
		assertThat(component.mode()).isEqualTo("dev");
	}

	/*
		This test uses an alternative defined in a module.
	 */
	@Test
	public void should_use_alternative_defined_in_modules() {
		Factory factory = Factory.newInstance();
		TestAlternativesFromModule.Calculation component = factory.getComponent(TestAlternativesFromModule.Calculation.class);
		assertThat(component.calculate(2, 3)).isEqualTo(5);

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(TestAlternativesFromModule.Calculation.class);
		assertThat(component.calculate(2, 3)).isEqualTo(6);
	}

	/*
		This test uses an alternative defined in a module, and the referenced component use a Named annotation
	 */
	@Test
	public void should_use_alternative_defined_in_modules_for_named_components() {
		Factory factory = Factory.newInstance();
		TestAlternativesFromModule.Flag component = factory.getComponent(TestAlternativesFromModule.Flag.class);
		assertThat(component.value()).isEqualTo(true);

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(TestAlternativesFromModule.Flag.class);
		assertThat(component.value()).isEqualTo(false);
	}

	/*
		This test defines two alternatives, with different priorities, check that the higher is used.
	 */
	@Test
	public void should_use_alternative_with_higher_priority() {
		Factory factory = Factory.newInstance();
		TestAlternativesFromModule.Priority component = factory.getComponent(TestAlternativesFromModule.Priority.class);
		assertThat(component.value()).isEqualTo(Integer.MAX_VALUE);

		overrideComponents().set("restx.test.alternatives", "true");

		factory = Factory.newInstance();
		component = factory.getComponent(TestAlternativesFromModule.Priority.class);
		assertThat(component.value()).isEqualTo(Integer.MIN_VALUE);
	}
}
