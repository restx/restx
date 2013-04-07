package restx.factory;

import restx.admin.AdminPage;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 4/7/13
 * Time: 2:59 PM
 */
@Module
public class FactoryAdminModule {
    @Provides @Named("Factory")
    public AdminPage getFactoryAdminPage() {
        return new AdminPage("/@/ui/factory/", "Factory");
    }
}
