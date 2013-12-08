package samplest.settings;

import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Module;
import restx.factory.Provides;

/**
 * Date: 8/12/13
 * Time: 22:06
 */
@Module
public class SettingsModule {
    @Provides
    public ConfigSupplier myConfigSupplier(ConfigLoader configLoader) {
        // Load settings.properties in samplest.settings package as a set of config entries
        return configLoader.fromResource("samplest/settings/settings");
    }
}
