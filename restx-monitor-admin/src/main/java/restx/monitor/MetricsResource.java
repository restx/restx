package restx.monitor;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

import java.util.Map;

/**
 * Date: 17/11/13
 * Time: 01:03
 */
@RestxResource(group = "restx-admin") @Component
public class MetricsResource {
    private final MetricRegistry metrics;

    public MetricsResource(MetricRegistry metrics) {
        this.metrics = metrics;
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
}
