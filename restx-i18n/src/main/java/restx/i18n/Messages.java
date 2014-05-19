package restx.i18n;

import java.util.Locale;

/**
 * Messages allow to access to internationalized messages, similar to what a ResourceBundle do, but with
 * an interpolation mechanism using a mustache like format, making it easier to use in Javascript client too.
 */
public interface Messages {
    /**
     * Return the list of message keys available in this Messages instance.
     *
     * @return the list of keys, as an Iterable
     */
    Iterable<String> keys();

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
