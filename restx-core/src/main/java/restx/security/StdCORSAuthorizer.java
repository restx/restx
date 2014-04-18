package restx.security;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 1:29 PM
 */
public class StdCORSAuthorizer implements CORSAuthorizer {
    private static final Logger logger = LoggerFactory.getLogger(StdCORSAuthorizer.class);

    public static class Builder {
        private Predicate<CharSequence> originMatcher = Predicates.alwaysTrue();
        private Predicate<CharSequence> pathMatcher = Predicates.alwaysTrue();
        private ImmutableCollection<String> allowedMethods = ImmutableSet.of("GET");
        private ImmutableCollection<String> allowedHeaders = ImmutableSet.of();
        private Optional<Boolean> allowCredentials = Optional.absent();
        private int maxAge = 1728000;

        public Builder setOriginMatcher(Predicate<CharSequence> originMatcher) {
            this.originMatcher = originMatcher;
            return this;
        }

        public Builder setPathMatcher(Predicate<CharSequence> pathMatcher) {
            this.pathMatcher = pathMatcher;
            return this;
        }

        public Builder setAllowedMethods(ImmutableCollection<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
            return this;
        }

        public Builder setAllowedHeaders(ImmutableCollection<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
            return this;
        }

        public Builder setAllowCredentials(Optional<Boolean> allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }

        public Builder setMaxAge(final int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public StdCORSAuthorizer build() {
            return new StdCORSAuthorizer(originMatcher, pathMatcher, allowedMethods, allowedHeaders, allowCredentials, maxAge);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Predicate<CharSequence> originMatcher;
    private final Predicate<CharSequence> pathMatcher;
    private final ImmutableCollection<String> allowedMethods;
    private final ImmutableCollection<String> allowedHeaders;
    private final Optional<Boolean> allowCredentials;
    private final int maxAge;

    public StdCORSAuthorizer(Predicate<CharSequence> originMatcher, Predicate<CharSequence> pathMatcher,
                             ImmutableCollection<String> allowedMethods, ImmutableCollection<String> allowedHeaders,
                             Optional<Boolean> allowCredentials, int maxAge) {
        this.maxAge = maxAge;
        this.originMatcher = checkNotNull(originMatcher);
        this.pathMatcher = checkNotNull(pathMatcher);
        this.allowedMethods = checkNotNull(allowedMethods);
        this.allowedHeaders = checkNotNull(toLowerCase(allowedHeaders));
        this.allowCredentials = checkNotNull(allowCredentials);
    }

    private ImmutableCollection<String> toLowerCase(ImmutableCollection<String> strings) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String string : strings) {
            builder.add(string.toLowerCase(Locale.ENGLISH));
        }
        return builder.build();
    }

    @Override
    public Optional<CORS> checkCORS(RestxRequest request, String origin, String method, String restxPath) {
        if (originMatcher.apply(origin) && pathMatcher.apply(restxPath)) {
            if (!Iterables.contains(allowedMethods, method)) {
                logger.debug("CORS request not accepted by {}: method not allowed {}\n" +
                        "REQUEST => {}", this, method, request);
                return Optional.of(CORS.reject());
            }

            if (!checkAllowed(request, "Access-Control-Request-Methods", allowedMethods)) {
                return Optional.of(CORS.reject());
            }
            if (!checkAllowed(request, "Access-Control-Request-Headers", allowedHeaders)) {
                return Optional.of(CORS.reject());
            }

            return Optional.of(CORS.accept(origin, allowedMethods, allowedHeaders, allowCredentials, maxAge));
        }
        return Optional.absent();
    }

    private boolean checkAllowed(RestxRequest request, String headerName, ImmutableCollection<String> allowed) {
        Optional<String> requestProperty = request.getHeader(headerName);
        if (requestProperty.isPresent()) {
            for (String s : Splitter.on(',').trimResults().split(requestProperty.get())) {
                if (!allowed.contains(s.toLowerCase(Locale.ENGLISH))) {
                    logger.debug("CORS request not accepted by {}: {} not allowed: {}\nREQUEST => {}", this, headerName, s, request);
                    return false;
                }
            }

        }
        return true;
    }

    @Override
    public String toString() {
        return "StdCORSAuthorizer{" +
                "originMatcher=" + originMatcher +
                ", pathMatcher=" + pathMatcher +
                ", allowedMethods=" + allowedMethods +
                ", allowedHeaders=" + allowedHeaders +
                ", allowCredentials=" + allowCredentials +
                '}';
    }
}
