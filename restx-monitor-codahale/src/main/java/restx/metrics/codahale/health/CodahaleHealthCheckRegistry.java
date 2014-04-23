package restx.metrics.codahale.health;

import restx.common.metrics.api.health.HealthCheck;
import restx.common.metrics.api.health.HealthCheckRegistry;

public class CodahaleHealthCheckRegistry implements HealthCheckRegistry {

    com.codahale.metrics.health.HealthCheckRegistry codahaleHealthCheckRegistry = new com.codahale.metrics.health.HealthCheckRegistry();

    @Override
    public void register(String name, HealthCheck healthCheck) {
        codahaleHealthCheckRegistry.register(name, new CodahaleHealthCheckAdapter(healthCheck));
    }

    public com.codahale.metrics.health.HealthCheckRegistry getCodahaleHealthCheckRegistry() {
        return codahaleHealthCheckRegistry;
    }
}
