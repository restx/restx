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
            doc = "enable or disable the storage of restx stats on the file system, allowing " +
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

    @SettingsKey(
            key = "restx.stats.share.enable", defaultValue = "true",
            doc = "enable or disable the sharing of restx stats, allowing the community" +
                    " to collect statistics of RESTX usage. See http://restx.io/stats.html for details.")
    boolean shareEnable();

    @SettingsKey(
            key = "restx.stats.share.url", defaultValue = "http://stats.restx.io/api/v1/stats",
            doc = "the URL on which stats should be shared (using a POST)")
    String shareURL();

    @SettingsKey(
            key = "restx.stats.share.period", defaultValue = "600000",
            doc = "the period, in ms, at which stats are shared")
    long sharePeriod();
}
