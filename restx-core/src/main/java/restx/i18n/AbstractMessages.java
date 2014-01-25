package restx.i18n;

import java.util.Locale;

/**
 * Date: 25/1/14
 * Time: 14:38
 */
public abstract class AbstractMessages implements Messages {
    @Override
    public String getMessage(String key) {
        return getMessage(key, MessageParams.empty(), Locale.getDefault());
    }

    @Override
    public String getMessage(String key, Locale locale) {
        return getMessage(key, MessageParams.empty(), locale);
    }

    @Override
    public String getMessage(String key, MessageParams params) {
        return getMessage(key, params, Locale.getDefault());
    }
}
