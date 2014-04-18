package restx.security;

import com.google.common.base.Optional;
import restx.RestxRequest;

import java.util.Collection;

/**
* User: xavierhanin
* Date: 4/1/13
* Time: 10:29 PM
*/
public class AcceptedCORS extends CORS {
    private final String origin;
    private final Collection<String> methods;
    private final Collection<String> headers;
    private final Optional<Boolean> credentials;
    private final int maxAge;

    AcceptedCORS(String origin, Collection<String> methods, Collection<String> headers, Optional<Boolean> credentials, int maxAge) {
        this.headers = headers;
        this.credentials = credentials;
        this.origin = origin;
        this.methods = methods;
        this.maxAge = maxAge;
    }


    public boolean isAccepted() {
        return true;
    }

    public String getOrigin() {
        return origin;
    }

    public Collection<String> getMethods() {
        return methods;
    }

    public Collection<String> getHeaders() {
        return headers;
    }

    public Optional<Boolean> getAllowCredentials() {
        return credentials;
    }

    public int getMaxAge() {
        return maxAge;
    }
}
