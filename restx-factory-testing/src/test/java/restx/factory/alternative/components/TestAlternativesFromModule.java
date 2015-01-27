package restx.factory.alternative.components;

import javax.inject.Named;
import restx.factory.Alternative;
import restx.factory.Component;
import restx.factory.Module;
import restx.factory.Provides;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Module
public class TestAlternativesFromModule {

	public static interface Calculation {
		int calculate(int a, int b);
	}

	@Provides
	public Calculation addition() {
		return new Calculation() {
			@Override
			public int calculate(int a, int b) {
				return a + b;
			}
		};
	}

	@Alternative(to = Calculation.class, named = "addition")
	@When(name = "restx.test.alternatives", value = "true")
	public Calculation multiplication() {
		return new Calculation() {
			@Override
			public int calculate(int a, int b) {
				return a * b;
			}
		};
	}

	public static interface Flag {
		boolean value();
	}

	@Provides
	@Named("SomeFlag")
	public Flag alwaysTrue() {
		return new Flag() {
			@Override
			public boolean value() {
				return true;
			}
		};
	}

	@Alternative(to = Flag.class, named = "SomeFlag")
	@When(name = "restx.test.alternatives", value = "true")
	public Flag alwaysFalse() {
		return new Flag() {
			@Override
			public boolean value() {
				return false;
			}
		};
	}

	public static interface Priority {
		int value();
	}

	@Provides
	public Priority priority() {
		return new Priority() {
			@Override
			public int value() {
				return Integer.MAX_VALUE;
			}
		};
	}

	@Alternative(to = Priority.class, named = "priority", priority = -2000)
	@When(name = "restx.test.alternatives", value = "true")
	public Priority nilPriority() {
		return new Priority() {
			@Override
			public int value() {
				return 0;
			}
		};
	}


	@Alternative(to = Priority.class, named = "priority", priority = -3000)
	@When(name = "restx.test.alternatives", value = "true")
	public Priority minPriority() {
		return new Priority() {
			@Override
			public int value() {
				return Integer.MIN_VALUE;
			}
		};
	}
}
