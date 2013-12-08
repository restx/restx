package samplest.settings;

import com.google.common.base.Optional;
import restx.config.Settings;
import restx.config.SettingsKey;

/**
 * Date: 11/11/13
 * Time: 17:33
 */
@Settings
public interface MySettings {
    @SettingsKey(key = "example.key1")
    Optional<String> key1();
    @SettingsKey(key = "example.key2", defaultValue = "MyValue2")
    String key2();
    @SettingsKey(key = "example.key3", defaultValue = "MyValue3",
            doc = "This is an example key 3")
    String key3();
}
