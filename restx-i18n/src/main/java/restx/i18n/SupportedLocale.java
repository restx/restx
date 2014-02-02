package restx.i18n;

import java.util.Locale;

/**
 * A locale that is supported by the application.
 *
 * An application should provide all its supported locales as components.
 *
 * They can then be injected with multi injection support.
 *
 */
public class SupportedLocale {
    private final Locale locale;

    public SupportedLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return "SupportedLocale{" +
                "locale=" + locale +
                '}';
    }
}
