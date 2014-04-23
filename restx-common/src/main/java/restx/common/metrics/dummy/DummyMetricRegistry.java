package restx.common.metrics.dummy;

import restx.common.metrics.api.MetricRegistry;
import restx.common.metrics.api.Timer;

public class DummyMetricRegistry implements MetricRegistry {
    @Override
    public Timer timer(String name) {
        return new DummyTimer(name);
    }
}
