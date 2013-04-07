package restx.exceptions;

import restx.admin.AdminPage;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 4/7/13
 * Time: 2:59 PM
 */
@Module
public class ErrorsAdminModule {
    @Provides @Named("Errors")
    public AdminPage getErrorAdminPage() {
        return new AdminPage("/@/ui/errors/", "Errors");
    }
}
