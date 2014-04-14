package restx.monitor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.ThreadDump;
import com.google.common.collect.ImmutableMap;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.metrics.codahale.CodahaleMetricRegistry;
import restx.metrics.codahale.health.CodahaleHealthCheckRegistry;

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

    public MetricsResource(restx.common.metrics.api.MetricRegistry metricRegistry, restx.common.metrics.api.health.HealthCheckRegistry healthCheckRegistry) {
        if (!(metricRegistry instanceof CodahaleMetricRegistry)){
            throw new IllegalStateException("restx-monitor-admin expects that module restx-monitor-codahale is loaded");
        }
        CodahaleMetricRegistry codahaleMetricRegistry = (CodahaleMetricRegistry) metricRegistry;
        CodahaleHealthCheckRegistry codahaleHealthCheckRegistry = (CodahaleHealthCheckRegistry) healthCheckRegistry;

        this.metrics = codahaleMetricRegistry.getCodahaleMetricRegistry();
        this.healthChecks = codahaleHealthCheckRegistry.getCodahaleHealthCheckRegistry();
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
