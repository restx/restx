package restx.security;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.factory.*;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 9:33 AM
 */
public class CORSFilter implements RestxRoute {
    private final Iterable<CORSAuthorizer> authorizers;

    public CORSFilter(Iterable<CORSAuthorizer> authorizers) {
        this.authorizers = authorizers;
    }

    @Override
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        Optional<String> acrMethod = req.getHeader("Access-Control-Request-Method");
        Optional<String> origin = req.getHeader("Origin");
        if ("OPTIONS".equals(req.getHttpMethod())
                && acrMethod.isPresent()) {
            CORS cors = checkCORS(req, origin.get(), acrMethod.get(), req.getRestxPath());
            if (cors.isAccepted()) {
                resp.setHeader("Access-Control-Allow-Origin", cors.getOrigin());
                resp.setHeader("Access-Control-Allow-Methods", Joiner.on(", ").join(cors.getMethods()));
                resp.setHeader("Access-Control-Max-Age", String.valueOf(cors.getMaxAge()));
            }
            return true;
        }
        if ("GET".equals(req.getHttpMethod())
                        && origin.isPresent()) {
            CORS cors = checkCORS(req, origin.get(), "GET", req.getRestxPath());
            if (cors.isAccepted()) {
                resp.setHeader("Access-Control-Allow-Origin", cors.getOrigin());
            }
            return ctx.proceed(req, resp);
        }

        return false;
    }

    private CORS checkCORS(RestxRequest request, String origin, String method, String restxPath) {
        for (CORSAuthorizer authorizer : authorizers) {
            Optional<CORS> cors = authorizer.checkCORS(request, origin, method, restxPath);
            if (cors.isPresent()) {
                return cors.get();
            }
        }
        return CORS.reject();
    }

    public static class CORS {
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

        private CORS(boolean accepted, String origin, Iterable<String> methods, int maxAge) {
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

}
