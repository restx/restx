package restx.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
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

    /** uuid generator used to generate mahcine id */
    private final UUIDGenerator uuidGenerator;
    /** object mapper used to persist and load stats */
    private final ObjectMapper objectMapper;
    /** the stats collected by this collector */
    private final RestxStats stats;
    /** the rough startup time of this server */
    private final long startupTime;
    /** the total uptime collected during previous run */
    private final long previousTotalUptime;

    /** should stats be stored on disk to compute stats over multiple run */
    private final boolean storageEnabled;
    /** min period in ms between each storage */
    private final long storagePeriod;
    /** directory in which stats should be stored if enabled */
    private final File storageStatsDir;

    /** should stats be shared */
    private final boolean shareEnabled;
    /** min period in ms between each sharing */
    private final long sharePeriod;
    /** URL on which stats should be shared if enabled */
    private final String shareURL;

    /** the last time at which the stats have been touched */
    private volatile long lastTouchTime;
    /** the last time at which the stats have been stored */
    private volatile long lastStorageTime;
    /** the last time at which the stats have been shared */
    private volatile long lastShareTime;

    public RestxStatsCollector(
            @Named("app.name") Optional<String> appName,
            @Named("restx.server.type") Optional<String> serverType,
            @Named("restx.server.port") Optional<String> serverPort,
            AppSettings appSettings, RestxStatsSettings statsSettings,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // we don't inject it, we don't want during tests to use the playback version, it could perturb
        // tests since the number of calls to the generation depends on the presence of machine id
        // in the file system, which may vary from one test to another
        this.uuidGenerator = new DefaultUUIDGenerator();

        // load settings in private fields
        // this avoid accessing the settings through the underlying config object
        storageEnabled = statsSettings.storageEnable();
        storageStatsDir = new File(statsSettings.storageDir().or(System.getProperty("user.home") + "/.restx/stats"));
        storagePeriod = statsSettings.storagePeriod();
        shareEnabled = statsSettings.shareEnable();
        shareURL = statsSettings.shareURL();
        sharePeriod = statsSettings.sharePeriod();


        if (storageEnabled && !storageStatsDir.exists()) {
            storageStatsDir.mkdirs();
        }

        stats = loadPreviousStatsIfAvailable(
                new RestxStats()

                // we set what makes up the stats id, to be able to load previous one if any
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

    /**
     * Get the current stats.
     *
     * This also updates the stats with latest heap size and uptime information.
     *
     * @return current stats.
     */
    public RestxStats getStats() {
        updateUptime();
        updateHeapSize();
        touch();
        return stats;
    }

    @Override
    public void start() {
        // making the collector auto startable ensure it is loaded only once even in DEV mode
        // the actual initial gathering is done in the constructor to avoid storing some injected fields just between
        // constructor and start() call

        if (shareEnabled) {
            logger.info("collecting and sharing stats enabled - see http://restx.io/stats.html for details.");
        }

        logger.debug("stats collection started - current stats {}", stats);
    }

    /**
     * This method is called by RestxStatsCollectionFilter, to collect stats about requests.
     *
     * @param req the incoming request
     * @param resp the outgoing response
     * @param stop a stopwatch which has measured the request / response handling time
     */
    final void notifyRequest(RestxRequest req, RestxResponse resp, Stopwatch stop) {
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

    /**
     * Touch is used to update the stats timestamp, and is also used to store and share the stats
     * if they haven't been stored or shared for the period specified (if enabled).
     *
     * We don't use a cron for that, it would require a dedicated thread, so we prefer less accurate
     * but also less impact on the application performance.
     */
    private void touch() {
        long now = System.currentTimeMillis();
        if (now - lastTouchTime > 100) {
            // we don't update timestamp too frequently to avoid creating new DateTime object too frequently
            stats.setTimestamp(new DateTime(now));
            lastTouchTime = now;

            maybeStoreStats(now);
            maybeShareStats(now);
        }
    }

    /**
     * Stores the stats if storage is enabled and if they haven't been stored for the specified period.
     *
     * It also updates the heap and uptime information before saving the stats.
     *
     * @param now current time
     */
    private void maybeStoreStats(long now) {
        if (storageEnabled && now - lastStorageTime > storagePeriod) {
            boolean shouldUpdate = false;
            synchronized (this) {
                if (now - lastStorageTime > storagePeriod) {
                    shouldUpdate = true;
                    lastStorageTime = now;
                }
            }

            if (shouldUpdate) {
                updateHeapSize();
                updateUptime();
                storeStats();
            }
        }
    }

    /**
     * Shares the stats if sharing is enabled and if they haven't been shared for the specified period.
     *
     * It also updates the heap and uptime information before sharing the stats.
     *
     * @param now current time
     */
    private void maybeShareStats(long now) {
        if (shareEnabled && now - lastShareTime > sharePeriod) {
            boolean shouldUpdate = false;
            synchronized (this) {
                if (now - lastShareTime > sharePeriod) {
                    shouldUpdate = true;
                    lastShareTime = now;
                }
            }

            if (shouldUpdate) {
                updateHeapSize();
                updateUptime();
                shareStats();
            }
        }
    }

    /**
     * Updates current and total uptime
     */
    private void updateUptime() {
        long currentUptime = System.currentTimeMillis() - startupTime;
        stats.setCurrentUptime(currentUptime);
        stats.setTotalUptime(previousTotalUptime + currentUptime);
    }

    private void updateHeapSize() {
        stats.setHeapSize(Runtime.getRuntime().totalMemory());
    }

    /**
     * Stores the stats on disk. Must not be called if storage is disabled.
     * storageEnabled check is not done to avoid double checking.
     */
    private synchronized void storeStats() {
        File statsFile = getStatsFile(stats.getStatsId());
        try {
            objectMapper.writer().writeValue(statsFile, stats);
        } catch (Exception e) {
            logger.info("saving stats to {} failed. Exception: {}", statsFile, e.getMessage());
        }
    }

    /**
     * Share the stats to share URL. Must not be called if sharing is disabled.
     * shareEnabled check is not done to avoid double checking.
     */
    private void shareStats() {
        try {
            int code = HttpRequest.post(shareURL)
                    .connectTimeout(5000)
                    .readTimeout(5000)
                    .send(objectMapper.writer().writeValueAsString(stats).getBytes(Charsets.UTF_8))
                    .code();
            if (code >= 400) {
                logger.info("sharing stats on {} failed. Response code: {}", shareURL, code);
            }
        } catch (Exception e) {
            logger.info("sharing stats on {} failed. Exception: {}", shareURL, e.getMessage());
        }
    }

    /**
     * Loads previous stats from disk if available and storage is enabled.
     *
     * @param stats the stats with same id as the one to load.
     *
     * @return loaded stats if available, or given stats if not loaded.
     */
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

    /**
     * Returns an ID to identify this machine.
     *
     * Instead of making a guess based on MAC address and other related stuff, we only rely on a stored UUID to make
     * it faster. It means that when storage is disabled, the machine id will be different at each run, preventing to
     * consolidate information at machine level.
     *
     * @return the machine id, which may be different at each call.
     */
    private synchronized String getMachineId() {
        if (!storageEnabled) {
            return uuidGenerator.doGenerate();
        }

        File machineIdFile = new File(storageStatsDir, "machineId");
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

    /**
     * Fill the structure of the map of RequestStat, so that we don't have to concurrently check for insertions
     * when in use.
     *
     * Stats for HTTP methods which are not present in the map after this call won't be collected.
     */
    private void fillPerHttpMethodRequestStats() {
        Map<String,RequestStats> requestStats = stats.getRequestStats();
        for (String httpMethod : new String[]{"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE", "CONNECT"}) {
            if (!requestStats.containsKey(httpMethod)) {
                requestStats.put(httpMethod, new RequestStats().setHttpMethod(httpMethod));
            }
        }
    }

    private File getStatsFile(String statsId) {
        return new File(storageStatsDir, "restx-stats-" + statsId + ".json");
    }

    @Override
    public void close() throws Exception {
        if (storageEnabled) {
            stats.setTimestamp(DateTime.now());
            updateHeapSize();
            updateUptime();
            storeStats();
        }
        if (shareEnabled) {
            stats.setTimestamp(DateTime.now());
            updateHeapSize();
            updateUptime();
            shareStats();
        }
    }
}
