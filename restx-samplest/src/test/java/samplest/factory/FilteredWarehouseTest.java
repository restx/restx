package samplest.factory;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Enumeration;
import restx.common.RestxConfig;
import restx.factory.Factory;
import restx.factory.FilteredWarehouse;

/**
 * @author apeyrard
 */
public class FilteredWarehouseTest {

	/**
	 * Clears properties starting with "restx.test." before every test method.
	 */
	@Rule
	public TestRule clearProperties = new TestRule() {
		@Override
		public Statement apply(final Statement base, Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Enumeration<?> properties = System.getProperties().propertyNames();
					while (properties.hasMoreElements()) {
						String key = (String) properties.nextElement();
						if (key.startsWith("restx.test.")) {
							System.clearProperty(key);
						}
					}
					base.evaluate();
				}
			};
		}
	};

	/*
		The two tests above permit to validate that RestxConfig and String component
		are built only once, and inherited from the main factory warehouse.
		Except if we use a FilteredWarehouse to wrap the main factory's warehouse, and
		to filter RestxConfig and String classes.
	 */

	@Test
	public void should_inherit_restx_config_and_ignore_new_value() {
		// create a property that will be transformed into a ConfigElement
		System.setProperty("restx.test.foo", "where is Waldo ?");

		// create a factory, and try to get some settings, property must be found
		Factory mainFactory = Factory.builder()
				.addFromServiceLoader()
				.build();
		assertThat(mainFactory.getComponent(TestSettingsFoo.class).foo()).isEqualTo("where is Waldo ?");
		assertThat(mainFactory.getComponent(TestSettingsBar.class).bar()).isEqualTo("bar");

		// change the value of bar
		System.setProperty("restx.test.bar", "in the top right corner");

		// create a sub factory
		Factory subFactory = Factory.builder()
				.addFromServiceLoader()
				.addWarehouseProvider(mainFactory.getWarehouse())
				.build();

		assertThat(subFactory.getComponent(TestSettingsFoo.class).foo()).isEqualTo("where is Waldo ?");
		assertThat(subFactory.getComponent(TestSettingsBar.class).bar()).isEqualTo("bar");
	}

	@Test
	public void should_filter_restx_config_and_initialize_a_new_one() {
		// create a property that will be transformed into a ConfigElement
		System.setProperty("restx.test.foo", "where is Waldo ?");

		// create a factory, and try to get some settings, property must be found
		Factory mainFactory = Factory.builder()
				.addFromServiceLoader()
				.build();
		assertThat(mainFactory.getComponent(TestSettingsFoo.class).foo()).isEqualTo("where is Waldo ?");
		assertThat(mainFactory.getComponent(TestSettingsBar.class).bar()).isEqualTo("bar");

		// change the value of bar
		System.setProperty("restx.test.bar", "in the top right corner");

		// create a sub factory
		Factory subFactory = Factory.builder()
				.addFromServiceLoader()
				.addWarehouseProvider(
						FilteredWarehouse.excludingClasses(mainFactory.getWarehouse(),
								RestxConfig.class, String.class, TestSettingsFooConfig.class, TestSettingsBarConfig.class))
				.build();

		assertThat(subFactory.getComponent(TestSettingsFoo.class).foo()).isEqualTo("where is Waldo ?");
		assertThat(subFactory.getComponent(TestSettingsBar.class).bar()).isEqualTo("in the top right corner");
	}
}
