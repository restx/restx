package restx.factory.conditional.components;

import javax.inject.Named;
import restx.factory.Module;
import restx.factory.Provides;

/**
 * @author apeyrard
 */
@Module
public class TestClassicModule {

	@Provides
	public TestInterfaces.Resolver prodResolver() {
		return new TestInterfaces.Resolver() {
			@Override
			public String resolve(String constraint) {
				return "prod:"+constraint;
			}
		};
	}

	@Provides @Named("db.type")
	public String dbType() {
		return "postgres";
	}
}
