package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.factory.Component;

import java.io.IOException;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 9:33 AM
 */
@Component
public class CORSFilter extends CORSHandler implements RestxFilter, RestxHandler {
    private static final Logger logger = LoggerFactory.getLogger(CORSFilter.class);
    private static final Collection<String> SIMPLE_METHODS = asList("GET", "HEAD", "POST", "OPTIONS", "DELETE");
    private final Iterable<CORSAuthorizer> authorizers;


    public CORSFilter(Iterable<CORSAuthorizer> authorizers) {
        this.authorizers = authorizers;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        Optional<String> origin = req.getHeader("Origin");
        if (origin.isPresent() && isSimpleCORSRequest(req)) {
            CORS cors = CORS.check(authorizers, req, origin.get(), req.getHttpMethod(), req.getRestxPath());
            if (cors.isAccepted()) {
                return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("*", req.getRestxPath(),
                        ImmutableMap.<String, String>of(), ImmutableMap.of("cors", cors)), this));
            } else {
                logger.info("Unauthorized CORS request; Origin={}; Method={}", origin.get(), req.getHttpMethod());
                return unauthorized(req);
            }
        }
        return Optional.absent();
    }

    protected boolean isSimpleCORSRequest(RestxRequest req) {
        if (!SIMPLE_METHODS.contains(req.getHttpMethod())) {
            return false;
        }
        Optional<String> origin = req.getHeader("Origin");
        if (!origin.isPresent()) {
            return false;
        }
        // same origin check.
        // see http://stackoverflow.com/questions/15512331/chrome-adding-origin-header-to-same-origin-request
        Optional<String> host = req.getHeader("Host");
        if (!host.isPresent()) {
            // no host header, can't check same origin
            return true;
        }
        if (origin.get().endsWith(host.get())) {
            logger.debug("Same Origin request not considered as CORS Request: {}", req);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        CORS cors = (CORS) match.getOtherParams().get("cors");
        if (cors != null && cors.isAccepted()) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Credentials", Boolean.TRUE.toString());
            resp.setHeader("Access-Control-Allow-Methods", SIMPLE_METHODS.toString());
            resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        }
        ctx.nextHandlerMatch().handle(req, resp, ctx);
    }

    public String toString() {
        return "CORSFilter";
    }

}
