package restx.monitor;

import com.codahale.metrics.DefaultObjectNameFactory;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ObjectNameFactory;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.AppSettings;
import restx.RestxContext;
import restx.factory.AutoStartable;
import restx.factory.Component;
import restx.metrics.codahale.CodahaleMetricRegistry;
import restx.metrics.codahale.health.CodahaleHealthCheckRegistry;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
    private final AppSettings appSettings;

    public MetricsConfiguration(restx.common.metrics.api.MetricRegistry metricRegistry, restx.common.metrics.api.health.HealthCheckRegistry healthCheckRegistry,
                                GraphiteSettings graphiteSettings, AppSettings appSettings) {

        if (!(metricRegistry instanceof CodahaleMetricRegistry)){
            throw new IllegalStateException("restx-monitor-admin expects that module restx-monitor-codahale is loaded");
        }
        CodahaleMetricRegistry codahaleMetricRegistry = (CodahaleMetricRegistry) metricRegistry;
        CodahaleHealthCheckRegistry codahaleHealthCheckRegistry = (CodahaleHealthCheckRegistry) healthCheckRegistry;

        this.metrics = codahaleMetricRegistry.getCodahaleMetricRegistry();
        this.healthChecks = codahaleHealthCheckRegistry.getCodahaleHealthCheckRegistry();
        this.graphiteSettings = graphiteSettings;
        this.appSettings = appSettings;
    }

    @Override
    public void start() {
        if (RestxContext.Modes.PROD.equals(appSettings.mode())
                || RestxContext.Modes.DEV.equals(appSettings.mode())) {
            logger.info("registering Metrics JVM metrics");
            metrics.register("jvm.memory", new MemoryUsageGaugeSet());
            metrics.register("jvm.garbage", new GarbageCollectorMetricSet());
            metrics.register("jvm.threads", new ThreadStatesGaugeSet());
            metrics.register("jvm.files", new FileDescriptorRatioGauge());
            metrics.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

            healthChecks.register("threadLocks", new ThreadDeadlockHealthCheck());
        }

        if (RestxContext.Modes.PROD.equals(appSettings.mode())) {
            setupReporters();
        }
    }

    protected void setupReporters() {
        setupJmxReporter();

        setupGraphiteReporter();
    }

    protected void setupJmxReporter() {
        logger.info("Initializing Metrics JMX Reporter");
        final JmxReporter jmxReporter = JmxReporter.forRegistry(metrics)
                .createsObjectNamesWith(new DefaultObjectNameFactory() {
                    Pattern wildcards = Pattern.compile("[\\*\\?]");

                    @Override
                    public ObjectName createName(String type, String domain, String name) {
                        if (wildcards.matcher(name).find()) {
                            name = ObjectName.quote(name);
                        }

                        return super.createName(type, domain, name);
                    }
                }).build();
        jmxReporter.start();
    }

    protected void setupGraphiteReporter() {
        if (graphiteSettings.getGraphiteHost().isPresent()) {
            InetSocketAddress address = new InetSocketAddress(
                    graphiteSettings.getGraphiteHost().get(), graphiteSettings.getGraphitePort().or(2003));
            logger.info("Initializing Metrics Graphite reporting to {}", address);

            GraphiteReporter.Builder builder = GraphiteReporter.forRegistry(metrics);

            if (graphiteSettings.getPrefix().isPresent()) {
                String prefix = graphiteSettings.getPrefix().get();
                builder.prefixedWith(prefix);
                logger.info("Metrics Graphite prefix is set to '{}'", prefix);
            }

            GraphiteReporter graphiteReporter = builder
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(new Graphite(address));
            graphiteReporter.start(graphiteSettings.getFrequency().get(),
                    TimeUnit.valueOf(graphiteSettings.getFrequencyUnit().get()));
        }
    }
}
