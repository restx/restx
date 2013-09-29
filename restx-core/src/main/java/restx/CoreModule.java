package restx;

import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Module;
import restx.factory.Provides;

/**
 */
@Module
public class CoreModule {
    @Provides
    public ConfigSupplier appConfigLoader(ConfigLoader configLoader) {
        return configLoader.fromResource("restx/appConfig");
    }
}
