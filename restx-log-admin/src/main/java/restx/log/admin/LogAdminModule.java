package restx.log.admin;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.admin.AdminPage;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * Date: 17/11/13
 * Time: 10:20
 */
@Module
public class LogAdminModule {
    @Provides
    @Named("Log")
    public AdminPage getLogAdminPage() {
        return new AdminPage("/@/ui/log/", "Logs");
    }

    @Provides @Named("LogUIRoute")
    public ResourcesRoute getLogResourcesRoute() {
        return new ResourcesRoute("LogUIRoute", "/@/ui/log", "restx/log", ImmutableMap.of("", "index.html"));
    }
}
