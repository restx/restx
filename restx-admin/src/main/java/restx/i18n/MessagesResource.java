package restx.i18n;

import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.PUT;
import restx.annotations.RestxResource;
import restx.factory.Component;

import javax.inject.Named;
import java.io.IOException;
import java.util.*;

/**
 * Date: 25/1/14
 * Time: 16:34
 */
@RestxResource @Component
public class MessagesResource {
    private final Messages messages;
    private final Collection<SupportedLocale> supportedLocales;

    public MessagesResource(@Named("Messages") Messages messages, Collection<SupportedLocale> supportedLocales) {
        this.messages = messages;
        this.supportedLocales = supportedLocales;
    }

    @GET("/@/i18n/keys")
    public Iterable<String> keys() {
        return messages.keys();
    }

    @GET("/@/i18n/locales")
    public Iterable<String> locales() {
        Collection<String> locales = new ArrayList<>();
        for (SupportedLocale supportedLocale : supportedLocales) {
            String tag = supportedLocale.getLocale().toLanguageTag();
            locales.add(Locale.ROOT.toLanguageTag().equals(tag) ? "/" : tag);
        }
        return locales;
    }

    @GET("/@/i18n/messages/{locale}")
    public Map<String, String> messages(String locale) {
        Locale l = toLocale(locale);
        Map<String, String> m = new LinkedHashMap<>();
        for (String key : messages.keys()) {
            m.put(key, messages.getMessageTemplate(key, l));
        }
        return m;
    }

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
