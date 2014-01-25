package restx.i18n;

import java.util.Locale;

/**
 * Date: 25/1/14
 * Time: 14:27
 */
public interface Messages {
    Iterable<String> keys();
    String getMessageTemplate(String key, Locale locale);

    String getMessage(String key);
    String getMessage(String key, Locale locale);
    String getMessage(String key, MessageParams params);
    String getMessage(String key, MessageParams params, Locale locale);
}
