package restx.security;

import com.google.common.base.Optional;
import restx.RestxRequest;

import java.util.Collection;
import java.util.Collections;

/**
* User: xavierhanin
* Date: 4/1/13
* Time: 10:29 PM
*/
public abstract class CORS {
    static CORS check(Iterable<CORSAuthorizer> authorizers, RestxRequest request, String origin, String method, String restxPath) {
        for (CORSAuthorizer authorizer : authorizers) {
            Optional<CORS> cors = authorizer.checkCORS(request, origin, method, restxPath);
            if (cors.isPresent()) {
                return cors.get();
            }
        }
        return CORS.reject();
    }

    public static CORS reject() {
        return REJECTED_CORS;
    }

    public static CORS accept(String origin, Collection<String> methods) {
        return new AcceptedCORS(origin, methods, Collections.<String>emptyList(), Optional.<Boolean>absent(), 1728000);
    }
    public static CORS accept(String origin, Collection<String> methods, int maxAge) {
        return new AcceptedCORS(origin, methods, Collections.<String>emptyList(), Optional.<Boolean>absent(), maxAge);
    }
    public static CORS accept(String origin, Collection<String> methods, Collection<String> headers, int maxAge) {
        return new AcceptedCORS(origin, methods, headers, Optional.<Boolean>absent(), maxAge);
    }
    public static CORS accept(String origin, Collection<String> methods, Collection<String> headers, Optional<Boolean> credentials, int maxAge) {
        return new AcceptedCORS(origin, methods, headers, credentials, maxAge);
    }

    public abstract boolean isAccepted();

    private static final RejectedCORS REJECTED_CORS = new RejectedCORS();

    private final static class RejectedCORS extends CORS {
        public boolean isAccepted() {
            return false;
        }
    }

}
