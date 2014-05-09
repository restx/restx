package restx.stats;

import com.google.common.base.Optional;
import restx.config.Settings;
import restx.config.SettingsKey;

/**
 * Date: 4/5/14
 * Time: 14:29
 */
@Settings
public interface RestxStatsSettings {
    @SettingsKey(
            key = "restx.stats.storage.enable", defaultValue = "true",
            doc = "enable or disable the storage of restx stats on the file system, allowing" +
                    "to gather statistics over multiple run")
    boolean storageEnable();

    @SettingsKey(
            key = "restx.stats.storage.dir",
            doc = "the directory in which stats should be stored")
    Optional<String> storageDir();

    @SettingsKey(
            key = "restx.stats.storage.period", defaultValue = "300000",
            doc = "the period, in ms, at which stats are saved to disk")
    long storagePeriod();
}
