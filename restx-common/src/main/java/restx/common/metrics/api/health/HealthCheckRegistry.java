package restx.common.metrics.api.health;

public interface HealthCheckRegistry {

    void register(String name, HealthCheck healthCheck);

}
