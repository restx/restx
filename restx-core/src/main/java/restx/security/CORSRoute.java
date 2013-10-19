package restx.security;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.*;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 9:33 AM
 */
public class CORSRoute implements RestxFilter, RestxHandler {
    private final Iterable<CORSAuthorizer> authorizers;

    public CORSRoute(Iterable<CORSAuthorizer> authorizers) {
        this.authorizers = authorizers;
    }

    @Override
    public Optional<? extends RestxRouteMatch> match(RestxRequest req) {
        Optional<String> acrMethod = req.getHeader("Access-Control-Request-Method");
        Optional<String> origin = req.getHeader("Origin");
        if ("OPTIONS".equals(req.getHttpMethod())
                && acrMethod.isPresent()) {
            CORS cors = CORS.check(authorizers, req, origin.get(), acrMethod.get(), req.getRestxPath());
            if (cors.isAccepted()) {
                return Optional.of(new StdRestxRouteMatch(this, "*", req.getRestxPath(),
                        ImmutableMap.<String, String>of(), ImmutableMap.of("cors", cors)));
            }
        }
        return Optional.absent();
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        CORS cors = (CORS) match.getOtherParams().get("cors");
        resp.setHeader("Access-Control-Allow-Origin", cors.getOrigin());
        resp.setHeader("Access-Control-Allow-Methods", Joiner.on(", ").join(cors.getMethods()));
        resp.setHeader("Access-Control-Max-Age", String.valueOf(cors.getMaxAge()));
    }

    public String toString() {
        return "CORSRoute";
    }
}
