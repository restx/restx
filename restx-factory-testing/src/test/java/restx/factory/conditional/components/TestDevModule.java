package restx.factory.conditional.components;

import com.google.common.collect.ImmutableList;

import java.util.List;
import javax.inject.Named;
import restx.factory.Alternative;
import restx.factory.Module;
import restx.factory.Provides;
import restx.factory.When;

/**
 * @author apeyrard
 */
@Module(priority = -100)
@When(name = "my-mode", value = "dev")
public class TestDevModule {

	@Alternative(to = TestInterfaces.Resolver.class, named = "prodResolver")
	public TestInterfaces.Resolver devResolver() {
		return new TestInterfaces.Resolver() {
			@Override
			public String resolve(String constraint) {
				return "dev:"+constraint;
			}
		};
	}

	@Provides
	public TestInterfaces.Workspace workspace() {
		return new TestInterfaces.Workspace() {
			@Override
			public List<String> modules() {
				return ImmutableList.of("mod1", "mod2");
			}
		};
	}

	@Provides @Named("db.type")
	public String dbType() {
		return "derby";
	}
}
