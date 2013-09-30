package restx.config;

import restx.admin.AdminPage;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 */
@Module
public class ConfigAdminModule {
    @Provides @Named("Config")
    public AdminPage getErrorAdminPage() {
        return new AdminPage("/@/ui/config/", "Config");
    }
}
