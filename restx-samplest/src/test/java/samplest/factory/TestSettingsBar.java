package samplest.factory;

import restx.config.Settings;
import restx.config.SettingsKey;

/**
 * @author apeyrard
 */
@Settings
public interface TestSettingsBar {

	@SettingsKey(key = "restx.test.bar", defaultValue = "bar")
	String bar();
}
