package restx;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
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
    public Optional<String> appPackage() {
        return config.getString("restx.app.package");
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

    @Override
    public String cachedResources() {
        return config.getString("restx.cache.cachedResources").or("");
    }

    @Override
    public Optional<Boolean> autoCompile() {
        return config.getBoolean("restx.router.autocompile");
    }

    @Override
    public Optional<Boolean> hotCompile() {
        return config.getBoolean("restx.router.hotcompile");
    }

    @Override
    public Optional<Boolean> hotReload() {
        return config.getBoolean("restx.router.hotreload");
    }

    @Override
    public String mode() {
        return config.getString("restx.mode").get();
    }

    @Override
    public Optional<String> factoryLoadMode() {
        return Optional.fromNullable(Strings.emptyToNull(
                config.getString("restx.factory.load").or("")));
    }

	@Override
	public Optional<String> coldClasses() {
		return Optional.fromNullable(Strings.emptyToNull(
				config.getString("restx.cold.classes").or("")));
	}
}
