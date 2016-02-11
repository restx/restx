package restx.i18n.admin;

import restx.admin.AdminModule;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.i18n.Messages;
import restx.i18n.MutableMessages;
import restx.i18n.SupportedLocale;
import restx.security.RolesAllowed;

import javax.inject.Named;
import java.io.IOException;
import java.util.*;

/**
 * Date: 25/1/14
 * Time: 16:34
 */
@RestxResource(group = "restx-admin")
@Component
public class MessagesAdminResource {
    private final Messages messages;
    private final Collection<SupportedLocale> supportedLocales;

    public MessagesAdminResource(@Named("Messages") Messages messages, Collection<SupportedLocale> supportedLocales) {
        this.messages = messages;
        this.supportedLocales = supportedLocales;
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @GET("/@/i18n/keys")
    public Iterable<String> keys() {
        return messages.keys();
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @GET("/@/i18n/locales")
    public Iterable<String> locales() {
        Collection<String> locales = new ArrayList<>();
        for (SupportedLocale supportedLocale : supportedLocales) {
            String tag = supportedLocale.getLocale().toLanguageTag();
            locales.add(Locale.ROOT.toLanguageTag().equals(tag) ? "/" : tag);
        }
        return locales;
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @GET("/@/i18n/messages/{locale}")
    public Map<String, String> messages(String locale) {
        Locale l = toLocale(locale);
        Map<String, String> m = new LinkedHashMap<>();
        for (String key : messages.keys()) {
            m.put(key, messages.getMessageTemplate(key, l));
        }
        return m;
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @POST("/@/i18n/messages/{locale}")
    public void setMessage(String locale, Map<String, String> entries) {
        if (!(messages instanceof MutableMessages)) {
            throw new IllegalStateException(
                    "can't update messages: not a MutableMessages instance. Are you in PROD mode?");
        }

        try {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                ((MutableMessages) messages).setMessageTemplate(
                        entry.getKey(), entry.getValue(), toLocale(locale));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Locale toLocale(String locale) {
        return "/".equals(locale) ? Locale.ROOT : Locale.forLanguageTag(locale);
    }
}
