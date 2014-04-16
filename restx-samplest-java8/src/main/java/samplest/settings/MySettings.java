package samplest.settings;

import restx.config.Settings;
import restx.config.SettingsKey;

import java.util.Optional;

@Settings
public interface MySettings {
    @SettingsKey(key = "example.key1")
    Optional<String> key1();
    @SettingsKey(key = "example.key2", defaultValue = "key2")
    Optional<String> key2();
}
