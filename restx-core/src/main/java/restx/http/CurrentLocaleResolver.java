package restx.http;

import restx.RestxRequest;

import java.util.Locale;

/**
 * @author fcamblor
 */
public interface CurrentLocaleResolver {
    Locale guessLocale(RestxRequest request);
}
