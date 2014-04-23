package restx.monitor;

import restx.common.metrics.api.MetricRegistry;
import restx.common.metrics.api.health.HealthCheckRegistry;
import restx.factory.Factory;
import restx.factory.Module;
import restx.factory.Provides;
import restx.metrics.codahale.CodahaleMetricRegistry;
import restx.metrics.codahale.health.CodahaleHealthCheckRegistry;

import javax.inject.Named;

/**
 * User: manuel boillod
 * Date: 4/7/14
 * Time: 2:59 PM
 */
@Module
public class MonitorCodahaleModule {

    @Provides @Named(Factory.METRICS_REGISTRY)
    public MetricRegistry metricRegistry() {
        return new CodahaleMetricRegistry();
    }

    @Provides @Named(Factory.HEALTH_CHECK_REGISTRY)
    public HealthCheckRegistry healthCheckRegistry() {
        return new CodahaleHealthCheckRegistry();
    }

}
