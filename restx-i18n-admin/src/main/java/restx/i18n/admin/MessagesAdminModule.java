package restx.i18n.admin;

import restx.admin.AdminPage;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 */
@Module
public class MessagesAdminModule {
    @Provides @Named("Messages")
    public AdminPage getMessagesAdminPage() {
        return new AdminPage("/@/ui/messages/", "Messages");
    }
}
