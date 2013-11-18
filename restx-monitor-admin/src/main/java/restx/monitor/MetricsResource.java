package restx.monitor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.ThreadDump;
import com.google.common.collect.ImmutableMap;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * Date: 17/11/13
 * Time: 01:03
 */
@RestxResource(group = "restx-admin") @Component
public class MetricsResource {
    private final MetricRegistry metrics;
    private final HealthCheckRegistry healthChecks;
    private final ThreadDump threadDump;

    public MetricsResource(MetricRegistry metrics, HealthCheckRegistry healthChecks) {
        this.metrics = metrics;
        this.healthChecks = healthChecks;
        threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
    }

    @GET("/@/metrics")
    public Map metrics() {
        return ImmutableMap.of(
                "gauges", metrics.getGauges(),
                "timers", metrics.getTimers(),
                "meters", metrics.getMeters(),
                "counters", metrics.getCounters(),
                "histograms", metrics.getHistograms()
        );
    }

    @GET("/@/health-checks")
    public Map healthChecks() {
        return healthChecks.runHealthChecks();
    }

    @GET("/@/thread-dump")
    public String threadDump() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        threadDump.dump(out);
        return new String(out.toByteArray());
    }
}
