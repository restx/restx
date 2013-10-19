package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.*;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 9:33 AM
 */
public class CORSFilter implements RestxFilter, RestxHandler {
    private final Iterable<CORSAuthorizer> authorizers;

    public CORSFilter(Iterable<CORSAuthorizer> authorizers) {
        this.authorizers = authorizers;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        Optional<String> origin = req.getHeader("Origin");
        if ("GET".equals(req.getHttpMethod())
                        && origin.isPresent()) {
            CORS cors = CORS.check(authorizers, req, origin.get(), "GET", req.getRestxPath());
            if (cors.isAccepted()) {
                return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("*", req.getRestxPath(),
                        ImmutableMap.<String, String>of(), ImmutableMap.of("cors", cors)), this));
            }
        }
        return Optional.absent();
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", ((CORS) match.getOtherParams().get("cors")).getOrigin());
        ctx.nextHandlerMatch().handle(req, resp, ctx);
    }


    public String toString() {
        return "CORSFilter";
    }

}
