package restx.stats;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The datastructure used to store restx stats.
 *
 * The datastructure is not thread safe, it's the RestxStatsCollector responsibility to maintain a single object
 * of this class and ensure it is modified thread safely.
 */
public class RestxStats {

    /** The timestamp at which these stats were collected */
    private DateTime timestamp;

    // context information - do not change over time

    /** A hash of the application name */
    private String appNameHash;
    /** A UUID generated once and stored for the machine */
    private String machineId;
    /** the port on which the app is listening */
    private int port;

    /**
     * Server implementation used. Eg Jetty, Tomcat, SimpleFramework, ...
     * This can contain additional information such as if the server was started in embedded mode, the server version, ...
     */
    private String server;

    /**
     * OS and version on which this server is running.
     */
    private String os;
    /**
     * Java information: version, ...
     */
    private String java;

    /**
     * The total heap size on the server.
     */
    private long heapSize;

    /**
     * The version of restx used
     */
    private String restxVersion;
    /**
     * The restx mode used.
     */
    private String restxMode;

    /**
     * Attempt to detect data access layer used based on class availability:
     * jdbc, MongoDB, Couchbase, ElasticSearch, Redis, ...
     */
    private String dataAccessInfo;

    /**
     * An approximation of the total uptime of this server.
     */
    private long totalUptime;

    /**
     * The current uptime of this server, as of its last recording.
     */
    private long currentUptime;

    /**
     * the collection of collected stqts on request.
     */
    private Collection<RequestStats> requestStats = new ArrayList<>();

    public String getStatsId() {
        return appNameHash + "--" + machineId + "--" + port + "--" + restxMode;
    }

    /**
     * Some stats on HTTP requests handled by this server.
     * The stats are collected on 3 axes: HTTP method, HTTP status, and wether or not they were authenticated requests.
     */
    public static class RequestStats {
        /**
         * The HTTP method for which these stats were collected.
         * eg GET POST PUT DELETE ...
         */
        private String httpMethod;
        /**
         * The HTTP status for which these stats were collected.
         * eg 200 404 500 ...
         */
        private String httpStatus;

        /**
         * Wether or not the requests were authenticated.
         */
        private boolean authenticated;

        private long requestsCount;
        private long minDuration;
        private long maxDuration;
        private long avgDuration;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getAppNameHash() {
        return appNameHash;
    }

    public String getMachineId() {
        return machineId;
    }

    public int getPort() {
        return port;
    }

    public String getServer() {
        return server;
    }

    public String getOs() {
        return os;
    }

    public String getJava() {
        return java;
    }

    public long getHeapSize() {
        return heapSize;
    }

    public String getRestxVersion() {
        return restxVersion;
    }

    public String getRestxMode() {
        return restxMode;
    }

    public String getDataAccessInfo() {
        return dataAccessInfo;
    }

    public long getTotalUptime() {
        return totalUptime;
    }

    public long getCurrentUptime() {
        return currentUptime;
    }

    public Collection<RequestStats> getRequestStats() {
        return requestStats;
    }

    public RestxStats setTimestamp(final DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public RestxStats setAppNameHash(final String appNameHash) {
        this.appNameHash = appNameHash;
        return this;
    }

    public RestxStats setMachineId(final String machineId) {
        this.machineId = machineId;
        return this;
    }

    public RestxStats setPort(final int port) {
        this.port = port;
        return this;
    }

    public RestxStats setServer(final String server) {
        this.server = server;
        return this;
    }

    public RestxStats setOs(final String os) {
        this.os = os;
        return this;
    }

    public RestxStats setJava(final String java) {
        this.java = java;
        return this;
    }

    public RestxStats setHeapSize(final long heapSize) {
        this.heapSize = heapSize;
        return this;
    }

    public RestxStats setRestxVersion(final String restxVersion) {
        this.restxVersion = restxVersion;
        return this;
    }

    public RestxStats setRestxMode(final String restxMode) {
        this.restxMode = restxMode;
        return this;
    }

    public RestxStats setDataAccessInfo(final String dataAccessInfo) {
        this.dataAccessInfo = dataAccessInfo;
        return this;
    }

    public RestxStats setTotalUptime(final long totalUptime) {
        this.totalUptime = totalUptime;
        return this;
    }

    public RestxStats setCurrentUptime(final long currentUptime) {
        this.currentUptime = currentUptime;
        return this;
    }

    public RestxStats setRequestStats(final Collection<RequestStats> requestStats) {
        this.requestStats = requestStats;
        return this;
    }


}
