package restx.security;

import com.google.common.base.Optional;
import restx.RestxRequest;

/**
* User: xavierhanin
* Date: 4/1/13
* Time: 10:29 PM
*/
public class CORS {
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
        return new CORS(false, null, null, 0);
    }

    public static CORS accept(String origin, Iterable<String> methods) {
        return new CORS(true, origin, methods, 1728000);
    }
    public static CORS accept(String origin, Iterable<String> methods, int maxAge) {
        return new CORS(true, origin, methods, maxAge);
    }

    private boolean accepted;
    private String origin;
    private Iterable<String> methods;
    private int maxAge;

    CORS(boolean accepted, String origin, Iterable<String> methods, int maxAge) {
        this.accepted = accepted;
        this.origin = origin;
        this.methods = methods;
        this.maxAge = maxAge;
    }


    public boolean isAccepted() {
        return accepted;
    }

    public String getOrigin() {
        return origin;
    }

    public Iterable<String> getMethods() {
        return methods;
    }

    public int getMaxAge() {
        return maxAge;
    }
}
