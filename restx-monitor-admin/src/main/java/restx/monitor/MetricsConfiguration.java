package restx.monitor;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.AutoStartable;
import restx.factory.Component;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Date: 17/11/13
 * Time: 00:25
 */
@Component
public class MetricsConfiguration implements AutoStartable {
    private static final Logger logger = LoggerFactory.getLogger(MetricsConfiguration.class);

    private final MetricRegistry metrics;
    private final HealthCheckRegistry healthChecks;
    private final GraphiteSettings graphiteSettings;

    public MetricsConfiguration(MetricRegistry metrics, HealthCheckRegistry healthChecks, GraphiteSettings graphiteSettings) {
        this.metrics = metrics;
        this.healthChecks = healthChecks;
        this.graphiteSettings = graphiteSettings;
    }

    @Override
    public void start() {
        metrics.register("jvm.memory", new MemoryUsageGaugeSet());
        metrics.register("jvm.garbage", new GarbageCollectorMetricSet());
        metrics.register("jvm.threads", new ThreadStatesGaugeSet());
        metrics.register("jvm.files", new FileDescriptorRatioGauge());
        metrics.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

        healthChecks.register("threadLocks", new ThreadDeadlockHealthCheck());

        setupReporters();
    }

    protected void setupReporters() {
        final JmxReporter jmxReporter = JmxReporter.forRegistry(metrics).build();
        jmxReporter.start();

        setupGraphiteReporter();
    }

    private void setupGraphiteReporter() {
        if (graphiteSettings.getGraphiteHost().isPresent()) {
            InetSocketAddress address = new InetSocketAddress(
                    graphiteSettings.getGraphiteHost().get(), graphiteSettings.getGraphitePort().or(2003));
            logger.info("Initializing Metrics Graphite reporting to {}", address);
            GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(new Graphite(address));
            graphiteReporter.start(graphiteSettings.getFrequency().get(),
                    TimeUnit.valueOf(graphiteSettings.getFrequencyUnit().get()));
        }
    }
}
