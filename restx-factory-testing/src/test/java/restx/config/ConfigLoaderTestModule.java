package restx.config;

import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Module;
import restx.factory.Provides;

/**
 * User: xavierhanin
 * Date: 9/25/13
 * Time: 12:11 AM
 */
@Module
public class ConfigLoaderTestModule {
    @Provides
    public ConfigSupplier commonConfig(ConfigLoader loader) {
        return loader.fromResource("restx/common/config");
    }
}
