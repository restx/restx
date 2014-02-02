package restx.i18n;

import restx.AppSettings;
import restx.RestxContext;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Date: 2/2/14
 * Time: 08:31
 */
@Module(priority = 100)
public class I18nModule {
    @Provides
    @Named("Messages")
    public Messages messages(AppSettings appSettings) {
        if (RestxContext.Modes.PROD.equals(appSettings.mode())) {
            return new DefaultMessages("labels", StandardCharsets.UTF_8);
        } else {
            return new DefaultMutableMessages("labels", StandardCharsets.UTF_8);
        }
    }

    @Provides @Named("ROOT")
    public SupportedLocale rootSupportedLocale() {
        return new SupportedLocale(Locale.ROOT);
    }


    @Provides @Named("restx.i18n.labelsJsTemplate")
    public String labelsJsTemplate() {
        return "// RESTX Labels - customize this with restx.i18n.labelsJsTemplate named String\nwindow.labels = {LABELS};";
    }
}
