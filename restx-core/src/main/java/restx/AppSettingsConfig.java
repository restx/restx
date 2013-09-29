package restx;

import restx.common.RestxConfig;
import restx.factory.Component;

/**
 */
@Component(priority = 1000)
public class AppSettingsConfig implements AppSettings {
    private final RestxConfig config;

    public AppSettingsConfig(RestxConfig config) {
        this.config = config;
    }

    @Override
    public String appPackage() {
        return config.getString("restx.app.package").get();
    }

    @Override
    public String targetClasses() {
        return config.getString("restx.targetClasses").get();
    }

    @Override
    public String targetDependency() {
        return config.getString("restx.targetDependency").get();
    }

    @Override
    public String sourceRoots() {
        return config.getString("restx.sourceRoots").get();
    }

    @Override
    public String mainSources() {
        return config.getString("restx.mainSources").get();
    }

    @Override
    public String mainResources() {
        return config.getString("restx.mainResources").get();
    }
}
