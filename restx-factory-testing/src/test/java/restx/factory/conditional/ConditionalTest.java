package restx.factory.conditional;

import static org.assertj.core.api.Assertions.assertThat;
import static restx.factory.Factory.LocalMachines.overrideComponents;
import static restx.factory.Factory.LocalMachines.threadLocal;


import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Comparator;
import java.util.Set;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.NamedComponent;
import restx.factory.TestGreeting;
import restx.factory.conditional.components.TestConditionalComponent;
import restx.factory.conditional.components.TestInterfaces;
import restx.factory.conditional.components.TestModuleWithConditional;

/**
 * Test cases for conditionals.
 *
 * @author apeyrard
 */
public class ConditionalTest {

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
	public void should_provide_component_if_condition_is_verified() {
		Factory factory = Factory.newInstance();
		Set<TestModuleWithConditional.Pioneer> pioneers = factory.getComponents(TestModuleWithConditional.Pioneer.class);
		Iterable<String> names = Iterables.transform(pioneers, new Function<TestModuleWithConditional.Pioneer, String>() {
			@Override
			public String apply(TestModuleWithConditional.Pioneer pioneer) {
				return pioneer.name();
			}
		});
		assertThat(names).containsOnly("Marie Currie", "Charles Babbage");

		overrideComponents().set("period", "all");

		factory = Factory.newInstance();
		pioneers = factory.getComponents(TestModuleWithConditional.Pioneer.class);
		names = Iterables.transform(pioneers, new Function<TestModuleWithConditional.Pioneer, String>() {
			@Override
			public String apply(TestModuleWithConditional.Pioneer pioneer) {
				return pioneer.name();
			}
		});
		assertThat(names).containsOnly("Marie Currie", "Charles Babbage", "Alan Turing");
	}

	@Test
	public void should_provide_component_if_condition_is_verified_and_use_name_and_priorities() {
		Factory factory = Factory.newInstance();
		TestModuleWithConditional.Pioneer pioneer = factory.getComponent(Name.of(TestModuleWithConditional.Pioneer.class, "physics"));
		assertThat(pioneer.name()).isEqualTo("Marie Currie");

		overrideComponents().set("chauvinist", "true");

		factory = Factory.newInstance();
		pioneer = factory.getComponent(Name.of(TestModuleWithConditional.Pioneer.class, "physics"));
		assertThat(pioneer.name()).isEqualTo("Pierre Currie");
	}

	@Test
	public void should_use_modules_condition_on_all_its_components() {
		Factory factory = Factory.newInstance();
		TestInterfaces.Resolver resolver = factory.getComponent(TestInterfaces.Resolver.class);
		assertThat(resolver.resolve("foo")).isEqualTo("prod:foo");
		Optional<TestInterfaces.Workspace> workspace = factory.queryByClass(TestInterfaces.Workspace.class).findOneAsComponent();
		assertThat(workspace.isPresent()).isFalse();
		String dbType = factory.getComponent(Name.of(String.class, "db.type"));
		assertThat(dbType).isEqualTo("postgres");

		overrideComponents().set("my-mode", "dev");

		factory = Factory.newInstance();
		resolver = factory.getComponent(TestInterfaces.Resolver.class);
		assertThat(resolver.resolve("foo")).isEqualTo("dev:foo");
		workspace = factory.queryByClass(TestInterfaces.Workspace.class).findOneAsComponent();
		assertThat(workspace.isPresent()).isTrue();
		dbType = factory.getComponent(Name.of(String.class, "db.type"));
		assertThat(dbType).isEqualTo("derby");
	}

	@Test
	public void should_find_conditional_component_when_condition_is_satisfied() {
		Factory factory = Factory.newInstance();
		Optional<TestConditionalComponent> conditional = factory.queryByName(Name.of(TestConditionalComponent.class, "conditional"))
				.findOneAsComponent();
		assertThat(conditional.isPresent()).isFalse();

		overrideComponents().set("conditional.component", "allowed");

		factory = Factory.newInstance();
		conditional = factory.queryByName(Name.of(TestConditionalComponent.class, "conditional")).findOneAsComponent();
		assertThat(conditional.isPresent()).isTrue();
	}

	@Test
	public void should_use_asClass_parameter_for_conditional_components() {
		overrideComponents().set("allow.comparator", "true");

		Factory factory = Factory.newInstance();
		Optional<NamedComponent<Comparator>> one = factory.queryByClass(Comparator.class).findOne();
		assertThat(one.isPresent());
		assertThat(one.get().getName().getClazz()).isEqualTo(Comparator.class);
	}
}
