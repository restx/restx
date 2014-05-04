package restx.stats;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.admin.AdminPage;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

@Module
public class StatsAdminModule {
    @Provides
        @Named("Stats")
    public AdminPage getStatsAdminPage() {
        return new AdminPage("/@/ui/stats/", "Stats");
    }

    @Provides @Named("StatsUIRoute")
    public ResourcesRoute getStatsResourcesRoute() {
        return new ResourcesRoute("StatsUIRoute", "/@/ui/stats", "restx/stats/ui", ImmutableMap.of("", "index.html"));
    }
}
