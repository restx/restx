package restx.factory.conditional.components;

import javax.inject.Named;
import restx.factory.Module;
import restx.factory.Provides;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Module
public class TestModuleWithConditional {

	public static interface Pioneer {
		String name();
	}

	@Provides @Named("physics")
	public Pioneer currie() {
		return new Pioneer() {
			@Override
			public String name() {
				return "Marie Currie";
			}
		};
	}

	@Provides(priority = -100) @Named("physics")
	@When(name = "chauvinist", value = "true")
	public Pioneer pierreCurrie() {
		return new Pioneer() {
			@Override
			public String name() {
				return "Pierre Currie";
			}
		};
	}

	@Provides
	public Pioneer babbage() {
		return new Pioneer() {
			@Override
			public String name() {
				return "Charles Babbage";
			}
		};
	}

	@Provides
	@When(name = "period", value = "all")
	public Pioneer turing() {
		return new Pioneer() {
			@Override
			public String name() {
				return "Alan Turing";
			}
		};
	}
}
