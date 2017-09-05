package restx.http;

import restx.RestxRequest;
import java.util.Locale;

public class RequestBasedLocaleResolver implements CurrentLocaleResolver {
    @Override
    public Locale guessLocale(RestxRequest request) {
        return request.getLocale();
    }
}
