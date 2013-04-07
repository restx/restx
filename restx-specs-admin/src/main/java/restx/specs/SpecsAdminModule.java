package restx.specs;

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
public class SpecsAdminModule {
    @Provides
        @Named("Recorder")
    public AdminPage getRecorderAdminPage() {
        return new AdminPage("/@/ui/recorder/", "Recorder");
    }
}
