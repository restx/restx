package restx.security;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import restx.RestxRequest;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 1:29 PM
 */
public class StdCORSAuthorizer implements CORSAuthorizer {
    private final Predicate<CharSequence> originMatcher;
    private final Predicate<CharSequence> pathMatcher;
    private final Iterable<String> allowedMethods;

    public StdCORSAuthorizer(Predicate<CharSequence> originMatcher, Predicate<CharSequence> pathMatcher,
                             Iterable<String> allowedMethods) {
        this.originMatcher = originMatcher;
        this.pathMatcher = pathMatcher;
        this.allowedMethods = allowedMethods;
    }

    @Override
    public Optional<CORS> checkCORS(RestxRequest request, String origin, String method, String restxPath) {
        if (originMatcher.apply(origin) && pathMatcher.apply(restxPath)) {
            if (Iterables.contains(allowedMethods, method)) {
                return Optional.of(CORS.accept(origin, allowedMethods));
            } else {
                return Optional.of(CORS.reject());
            }
        }
        return Optional.absent();
    }

    @Override
    public String toString() {
        return "StdCORSAuthorizer{" +
                "originMatcher=" + originMatcher +
                ", pathMatcher=" + pathMatcher +
                ", allowedMethods=" + allowedMethods +
                '}';
    }
}
