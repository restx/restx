package restx.i18n;

import java.io.IOException;
import java.util.Locale;

/**
 * Date: 25/1/14
 * Time: 14:34
 */
public interface MutableMessages extends Messages {
    MutableMessages setMessageTemplate(String key, String messageTemplate, Locale locale) throws IOException;
}
