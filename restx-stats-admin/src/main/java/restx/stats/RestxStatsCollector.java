package restx.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.AppSettings;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.common.UUIDGenerator;
import restx.common.UUIDGenerator.DefaultUUIDGenerator;
import restx.common.Version;
import restx.factory.AutoStartable;
import restx.factory.Component;
import restx.stats.RestxStats.RequestStats;

import javax.inject.Named;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A collector of RestxStats.
 *
 * This class is responsible for collecting anonymous statistics on a restx server, save and share them.
 *
 * The stats collected are anonymized, have a look at RestxStats class to better see what information is sent,
 * and look at this class source to see how it is collected.
 *
 * Stats are shared to stats.restx.io to contribute these anonymous usage stats to the community. You can disable
 * that with `restx.stats.share.enable=false`. You can also get rid of the restx stats plugin altogether and stats collection
 * won't even be wired in your application.
 *
 * It tries to save stats to keep stats over time, but it needs a writable file system access for that. If writable
 * file system access is not available, or if you disable it using `restx.stats.storage.enable=false`, the collector
 * will try to use stats.restx.io to not only share but also as a way to store your stats between server runs.
 *
 */
@Component
public class RestxStatsCollector implements AutoStartable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RestxStatsCollector.class);

    private final RestxStatsSettings statsSettings;
    private final UUIDGenerator uuidGenerator;
    private final ObjectMapper objectMapper;
    private final RestxStats stats;
    private final long startupTime;
    private final long previousTotalUptime;
    private final boolean storageEnabled;

    private volatile long lastTouchTime;
    private volatile long lastStorageTime;

    public RestxStatsCollector(
            @Named("restx.appName") Optional<String> appName,
            @Named("restx.server.type") Optional<String> serverType,
            @Named("restx.server.port") Optional<String> serverPort,
            AppSettings appSettings, RestxStatsSettings statsSettings,
            ObjectMapper objectMapper) {
        this.statsSettings = statsSettings;
        this.objectMapper = objectMapper;

        // we don't inject it, we don't want during tests to use the playback version, it could perturb
        // tests since the number of calls to the generation depends on the presence of machine id
        // in the file system, which may vary from one test to another
        this.uuidGenerator = new DefaultUUIDGenerator();

        storageEnabled = statsSettings.storageEnable().equalsIgnoreCase("true");

        if (storageEnabled && !getStatsStorageDir().exists()) {
            getStatsStorageDir().mkdirs();
        }

        stats = loadPreviousStatsIfAvailable(new RestxStats()
                .setAppNameHash(Hashing.md5().hashString(appName.or("DEFAULT"), Charsets.UTF_8).toString())
                .setMachineId(getMachineId())
                .setRestxMode(appSettings.mode())
                .setPort(Integer.parseInt(serverPort.or("0"))))
        ;

        startupTime = System.currentTimeMillis();
        previousTotalUptime = stats.getTotalUptime();

        stats
                .setServer(serverType.or("unknown"))
                .setOs(getOs())
                .setJava(getJava())
                .setRestxVersion(Version.getVersion("io.restx", "restx-core"))
                .setDataAccessInfo(guessDataAccessInfo())
        ;

        fillPerHttpMethodRequestStats();
        updateHeapSize();
        touch();
    }

    public RestxStats getStats() {
        updateUptime();
        updateHeapSize();
        touch();
        return stats;
    }

    @Override
    public void start() {
        logger.debug("stats collection started - current stats {}", stats);
    }

    public final void notifyRequest(RestxRequest req, RestxResponse resp, Stopwatch stop) {
        RequestStats requestStats = stats.getRequestStats().get(req.getHttpMethod());
        if (requestStats != null) {
            requestStats.getRequestsCount().incrementAndGet();

            long duration = stop.elapsed(TimeUnit.MICROSECONDS);
            requestStats.getTotalDuration().addAndGet(duration);
            long minDuration;
            while ((minDuration = requestStats.getMinDuration().get()) > duration) {
                if (requestStats.getMinDuration().compareAndSet(minDuration, duration)) {
                    break;
                }
            }
            long maxDuration;
            while ((maxDuration = requestStats.getMaxDuration().get()) < duration) {
                if (requestStats.getMaxDuration().compareAndSet(maxDuration, duration)) {
                    break;
                }
            }
        }
        touch();
    }

    private void touch() {
        long now = System.currentTimeMillis();
        if (now - lastTouchTime > 100) {
            // we don't update timestamp too frequently to avoid creating new DateTime object too frequently
            stats.setTimestamp(new DateTime(now));
            lastTouchTime = now;

            maybeStoreStats(now);
        }
    }

    private void maybeStoreStats(long now) {
        if (storageEnabled && now - lastStorageTime > statsSettings.storagePeriod()) {
            boolean shouldUpdate = false;
            synchronized (this) {
                if (now - lastStorageTime > statsSettings.storagePeriod()) {
                    shouldUpdate = true;
                    updateHeapSize();
                    updateUptime();
                    lastStorageTime = now;
                }
            }

            if (shouldUpdate) {
                storeStats();
            }
        }
    }

    private void updateUptime() {
        long currentUptime = System.currentTimeMillis() - startupTime;
        stats.setCurrentUptime(currentUptime);
        stats.setTotalUptime(previousTotalUptime + currentUptime);
    }

    private void updateHeapSize() {
        stats.setHeapSize(Runtime.getRuntime().totalMemory());
    }

    private synchronized void storeStats() {
        try {
            File statsFile = getStatsFile(stats.getStatsId());
            objectMapper.writer().writeValue(statsFile, stats);
        } catch (Exception e) {
        }
    }

    private RestxStats loadPreviousStatsIfAvailable(RestxStats stats) {
        if (!storageEnabled) {
            return stats;
        }

        try {
            File statsFile = getStatsFile(stats.getStatsId());
            if (statsFile.exists()) {
                stats = objectMapper.reader(RestxStats.class).readValue(statsFile);
                lastStorageTime = statsFile.lastModified();
            }
            return stats;
        } catch (Exception e) {
            return stats;
        }
    }

    private String guessDataAccessInfo() {
        // TODO
        return "unknown";
    }

    private String getOs() {
        return System.getProperty("os.name") + ", " + System.getProperty("os.version") + ", " + System.getProperty("os.arch");
    }

    private String getJava() {
        return "VM: " + System.getProperty("java.vm.name") + ", " + System.getProperty("java.vm.version")
                + "; Version: " + System.getProperty("java.version") + ", " + System.getProperty("java.runtime.version");
    }

    private synchronized String getMachineId() {
        if (!storageEnabled) {
            return uuidGenerator.doGenerate();
        }

        File machineIdFile = new File(getStatsStorageDir(), "machineId");
        if (machineIdFile.exists()) {
            try {
                return Files.asCharSource(machineIdFile, Charsets.UTF_8).read();
            } catch (Exception e) {
                // ignored. we'll generate a new one
            }
        }

        String machineId = uuidGenerator.doGenerate();
        try {
            Files.asCharSink(machineIdFile, Charsets.UTF_8).write(machineId);
        } catch (Exception e) {
            // we were not able to store the machine id file, it means we'll generate a new one each time
        }

        return machineId;
    }

    private void fillPerHttpMethodRequestStats() {
        Map<String,RequestStats> requestStats = stats.getRequestStats();
        for (String httpMethod : new String[]{"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE", "CONNECT"}) {
            if (!requestStats.containsKey(httpMethod)) {
                requestStats.put(httpMethod, new RequestStats().setHttpMethod(httpMethod));
            }
        }
    }



    private File getStatsStorageDir() {
        return new File(statsSettings.storageDir().or(System.getProperty("user.home") + "/.restx/stats"));
    }

    private File getStatsFile(String statsId) {
        return new File(getStatsStorageDir(), "restx-stats-" + statsId + ".json");
    }

    @Override
    public void close() throws Exception {

    }
}
