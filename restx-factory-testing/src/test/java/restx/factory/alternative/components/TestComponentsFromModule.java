package restx.factory.alternative.components;

import javax.inject.Named;
import restx.factory.Module;
import restx.factory.Provides;

/**
 * This module permits to define some components used during alternatives tests.
 *
 * @author apeyrard
 */
@Module
public class TestComponentsFromModule {

	public static interface SomeInterface {
		String mode();
	}

	@Provides
	public SomeInterface production() {
		return new SomeInterface() {
			@Override
			public String mode() {
				return "production";
			}
		};
	}

	public static interface SomeOtherInterface {
		String mode();
	}

	@Provides
	@Named("restx.test.component.productionNamed")
	public SomeOtherInterface productionNamed() {
		return new SomeOtherInterface() {
			@Override
			public String mode() {
				return "production";
			}
		};
	}
}
