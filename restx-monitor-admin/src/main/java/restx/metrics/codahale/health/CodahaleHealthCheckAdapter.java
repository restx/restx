package restx.metrics.codahale.health;

import restx.common.metrics.api.health.HealthCheck;

public class CodahaleHealthCheckAdapter extends com.codahale.metrics.health.HealthCheck {

    HealthCheck healthCheck;

    public CodahaleHealthCheckAdapter(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    @Override
    protected Result check() throws Exception {
        healthCheck.check();
        return Result.healthy();
    }
}
