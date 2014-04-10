package restx;

import com.google.common.base.Optional;
import restx.config.SettingsKey;

/**
 */
public interface AppSettings {
    @SettingsKey(key = "restx.app.package")
    Optional<String> appPackage();

    @SettingsKey(key = "restx.targetClasses")
    String targetClasses();

    @SettingsKey(key = "restx.targetDependency")
    String targetDependency();

    @SettingsKey(key = "restx.sourceRoots")
    String sourceRoots();

    @SettingsKey(key = "restx.mainSources")
    String mainSources();

    @SettingsKey(key = "restx.mainResources")
    String mainResources();

    @SettingsKey(key = "restx.router.autocompile")
    Optional<Boolean> autoCompile();

    @SettingsKey(key = "restx.router.hotcompile")
    Optional<Boolean> hotCompile();

    @SettingsKey(key = "restx.router.hotreload")
    Optional<Boolean> hotReload();

    @SettingsKey(key = "restx.cache.cachedResources")
    String cachedResources();

    @SettingsKey(key = "restx.mode")
    String mode();

    @SettingsKey(key = "restx.factory.load")
    Optional<String> factoryLoadMode();
}
