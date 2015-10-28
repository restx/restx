package restx.i18n;

import restx.RestxRequest;
import restx.StdRequest;

import java.util.Locale;

/**
 * @author fcamblor
 */
public interface CurrentLocaleResolver {
    Locale guessLocale(RestxRequest request);
}
