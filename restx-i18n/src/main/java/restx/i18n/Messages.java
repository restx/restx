package restx.i18n;

import java.util.Locale;
import java.util.Map.Entry;

/**
 * Messages allow to access to internationalized messages, similar to what a ResourceBundle do, but with
 * an interpolation mechanism using a mustache like format, making it easier to use in Javascript client too.
 */
public interface Messages {
    /**
     * Return the list of message keys available in this Messages instance, for given Locale
     *
     * @param locale the locale in which the keys should be resolved.
     * @return the list of keys, as an Iterable
     */
    Iterable<String> keys(Locale locale);

    /**
     * Return the list of message keys available in this Messages instance.
     *
     * @return the list of keys, as an Iterable
     */
    Iterable<String> keys();
    /**
     * Return the list of message entries, i.e. association between a key and message template in the given locale.
     *
     * Note that the keys are obtained from the ROOT bundle, so if a key is defined in the ROOT message bundle but not
     * in the given locale it will appear in the list of entries.
     *
     * @param locale the locale in which the message templates should be returned.
     * @return the list of message entries, as an Iterable
     */
    Iterable<Entry<String, String>> entries(Locale locale);

    /**
     * Returns the message template associated to the given key in the given locale.
     *
     * The raw template is returned, without interpolation.
     *
     * @param key the key of the message to get.
     * @param locale the locale in which the message template should be returned.
     * @return the message template.
     */
    String getMessageTemplate(String key, Locale locale);

    /**
     * Returns the message associated with given key in default locale.
     *
     * @param key the message key.
     * @return the associated message.
     */
    String getMessage(String key);
    /**
     * Returns the message associated with given key in given locale.
     *
     * @param key the message key.
     * @param locale the locale in which the message should be returned.
     * @return the associated message.
     */
    String getMessage(String key, Locale locale);
    /**
     * Returns the message associated with given key in default locale,
     * interpolated with given MessageParams.
     *
     * @param key the message key.
     * @param params the message parameters to use in message interpolation.
     * @return the associated message.
     */
    String getMessage(String key, MessageParams params);
    /**
     * Returns the message associated with given key in given locale,
     * interpolated with given MessageParams.
     *
     * @param key the message key.
     * @param params the message parameters to use in message interpolation.
     * @param locale the locale in which the message should be returned.
     * @return the associated message.
     */
    String getMessage(String key, MessageParams params, Locale locale);
}
