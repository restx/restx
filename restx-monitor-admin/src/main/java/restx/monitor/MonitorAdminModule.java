package restx.monitor;

import restx.admin.AdminPage;
import restx.common.metrics.api.health.HealthCheckRegistry;
import restx.factory.Module;
import restx.factory.Provides;
import restx.metrics.codahale.health.CodahaleHealthCheckRegistry;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 4/7/13
 * Time: 2:59 PM
 */
@Module
public class MonitorAdminModule {
    @Provides
        @Named("Monitor")
    public AdminPage getMonitorAdminPage() {
        return new AdminPage("/@/ui/monitor/", "Monitor");
    }

    @Provides
    public HealthCheckRegistry healthCheckRegistry() {
        return new CodahaleHealthCheckRegistry();
    }

}
