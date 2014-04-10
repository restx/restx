package restx.metrics.codahale;

import restx.common.metrics.api.MetricRegistry;
import restx.common.metrics.api.Timer;

public class CodahaleMetricRegistry implements MetricRegistry{

    com.codahale.metrics.MetricRegistry codahaleMetricRegistry = new com.codahale.metrics.MetricRegistry();

    @Override
    public Timer timer(String name) {
        com.codahale.metrics.Timer timer = codahaleMetricRegistry.timer(name);
        return new CodahaleTimer(timer);
    }

    public com.codahale.metrics.MetricRegistry getCodahaleMetricRegistry() {
        return codahaleMetricRegistry;
    }
}
