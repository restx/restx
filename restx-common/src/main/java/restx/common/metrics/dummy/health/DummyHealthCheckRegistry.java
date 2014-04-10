package restx.common.metrics.dummy.health;

import restx.common.metrics.api.health.HealthCheck;
import restx.common.metrics.api.health.HealthCheckRegistry;

public class DummyHealthCheckRegistry implements HealthCheckRegistry {
    @Override
    public void register(String name, HealthCheck healthCheck) {
        //do nothing
    }
}
