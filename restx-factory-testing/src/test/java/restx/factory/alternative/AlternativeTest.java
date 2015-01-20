package restx.factory.alternative;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import restx.factory.Factory;
import restx.factory.Name;
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
	 * Removes all the system properties beginning with "restx.test.", in order to
	 * start every test methods with a clean state.
	 */
	@Before
	public void resetProperties() {
		Properties properties = System.getProperties();
		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith("restx.test.")) {
				properties.remove(propertyName);
			}
		}
	}

	/*
		This test uses the TestComponentSimple, and TestComponentSimpleAlternative. It uses the default alternative
		mechanism without using Named annotation.
	 */
	@Test
	public void should_use_alternative_for_basic_components() {
		Factory factory = Factory.builder().addFromServiceLoader().build();
		TestComponentSimple component = factory.getComponent(TestComponentSimple.class);
		assertThat(component.greet()).isEqualTo("hello");

		System.setProperty("restx.test.alternatives", "true");

		factory = Factory.builder().addFromServiceLoader().build();
		component = factory.getComponent(TestComponentSimple.class);
		assertThat(component.greet()).isEqualTo("bonjour");
	}

	/*
		This test uses the TestComponentNamed and TestComponentNamedAlternative, the alternative
		should be registered under the name defined in the reference component
	 */
	@Test
	public void should_use_alternative_for_named_components() {
		Factory factory = Factory.builder().addFromServiceLoader().build();
		TestComponentNamed component = factory.getComponent(Name.of(TestComponentNamed.class, "restx.test.component.speed"));
		assertThat(component.speed()).isEqualTo("slow");

		System.setProperty("restx.test.alternatives", "true");

		factory = Factory.builder().addFromServiceLoader().build();
		component = factory.getComponent(Name.of(TestComponentNamed.class, "restx.test.component.speed"));
		assertThat(component.speed()).isEqualTo("fast");
	}

	/*
		This test uses component based on an interface, the alternative reference the interface and force to be registered
		 with the same name	as the original component.
	 */
	@Test
	public void should_use_alternative_referencing_an_interface() {
		Factory factory = Factory.builder().addFromServiceLoader().build();
		TestComponentInterface component = factory.getComponent(Name.of(TestComponentInterface.class, "restx.test.component.name"));
		assertThat(component.name()).isEqualTo("original");

		System.setProperty("restx.test.alternatives", "true");

		factory = Factory.builder().addFromServiceLoader().build();
		component = factory.getComponent(Name.of(TestComponentInterface.class, "restx.test.component.name"));
		assertThat(component.name()).isEqualTo("alternative");
	}

	/*
		This test uses a component provided by a module, in order to create an alternative, it has to use the name of the
		method annotated with @Provides.
	 */
	@Test
	public void should_use_alternative_for_provided_component_using_the_method_as_name() {
		Factory factory = Factory.builder().addFromServiceLoader().build();
		TestComponentsFromModule.SomeInterface component = factory.getComponent(TestComponentsFromModule.SomeInterface.class);
		assertThat(component.mode()).isEqualTo("production");

		System.setProperty("restx.test.alternatives", "true");

		factory = Factory.builder().addFromServiceLoader().build();
		component = factory.getComponent(TestComponentsFromModule.SomeInterface.class);
		assertThat(component.mode()).isEqualTo("dev");
	}

	/*
		This test uses a named component provided by a module, in order to create an alternative, it has to use the same
		name as the component.
	 */
	@Test
	public void should_use_alternative_for_provided_named_component_using_same_name() {
		Factory factory = Factory.builder().addFromServiceLoader().build();
		TestComponentsFromModule.SomeOtherInterface component = factory.getComponent(
				Name.of(TestComponentsFromModule.SomeOtherInterface.class, "restx.test.component.productionNamed"));
		assertThat(component.mode()).isEqualTo("production");

		System.setProperty("restx.test.alternatives", "true");

		factory = Factory.builder().addFromServiceLoader().build();
		component = factory.getComponent(Name.of(TestComponentsFromModule.SomeOtherInterface.class, "restx.test.component.productionNamed"));
		assertThat(component.mode()).isEqualTo("dev");
	}
}
