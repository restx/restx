package restx.specs;

import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Module;
import restx.factory.Provides;

/**
 */
@Module
public class SpecModule {
    @Provides
    public ConfigSupplier specConfigSupplier(ConfigLoader configLoader) {
        return configLoader.fromResource("restx/specs/specConfig");
    }
}
