package restx.monitor;

import com.google.common.base.Optional;
import restx.config.Settings;
import restx.config.SettingsKey;

/**
 * Date: 17/11/13
 * Time: 00:32
 */
@Settings
public interface GraphiteSettings {
    @SettingsKey(key = "graphite.host", doc = "the host at which a graphite server is listening to collect metrics info" +
            " - set it to activate graphite reporting")
    Optional<String> getGraphiteHost();
    @SettingsKey(key = "graphite.port", doc = "the port at which a graphite server is listening to collect metrics info")
    Optional<Integer> getGraphitePort();

    @SettingsKey(key = "graphite.reporter.frequency", doc = "the frequency at which data is sent to graphite", defaultValue = "1")
    Optional<Integer> getFrequency();
    @SettingsKey(key = "graphite.reporter.frequency.unit", doc = "the unit in which frequency is expressed", defaultValue = "MINUTES")
    Optional<String> getFrequencyUnit();

    @SettingsKey(key = "graphite.prefix", doc = "the value prepended to all metrics names which are sent to graphite")
    Optional<String> getPrefix();
}
