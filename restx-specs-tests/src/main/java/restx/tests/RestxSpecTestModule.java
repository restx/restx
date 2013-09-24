package restx.tests;

import restx.common.ConfigLoader;
import restx.common.ConfigSupplier;
import restx.factory.Module;
import restx.factory.Provides;

/**
 * User: xavierhanin
 * Date: 9/25/13
 * Time: 12:28 AM
 */
@Module
public class RestxSpecTestModule {
    @Provides
    public ConfigSupplier specTestConfig(ConfigLoader loader) {
        return loader.fromResource("restx/tests/specConfig");
    }
}
