package samplest.factory;

import restx.common.RestxConfig;
import restx.config.Settings;
import restx.config.SettingsKey;
import restx.factory.Module;
import restx.factory.Provides;

/**
 * @author apeyrard
 */
@Settings
public interface TestSettingsFoo {

	@SettingsKey(key = "restx.test.foo", defaultValue = "foo")
	String foo();
}
