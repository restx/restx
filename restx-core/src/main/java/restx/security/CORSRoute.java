package restx.security;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.factory.Component;
import restx.http.HttpStatus;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 9:33 AM
 */
@Component
public class CORSRoute extends CORSHandler implements RestxRoute, RestxHandler {
    private static final Logger logger = LoggerFactory.getLogger(CORSRoute.class);

    private final Iterable<CORSAuthorizer> authorizers;

    public CORSRoute(Iterable<CORSAuthorizer> authorizers) {
        this.authorizers = authorizers;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        Optional<String> acrMethod = req.getHeader("Access-Control-Request-Method");
        if ("OPTIONS".equals(req.getHttpMethod())
                && acrMethod.isPresent()) {
            Optional<String> origin = req.getHeader("Origin");
            CORS cors = CORS.check(authorizers, req, origin.get(), acrMethod.get(), req.getRestxPath());
            if (cors.isAccepted()) {
                return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("*", req.getRestxPath(),
                        ImmutableMap.<String, String>of(), ImmutableMap.of("cors", cors)), this));
            } else {
                logger.info("Unauthorized pre-flight CORS request; Origin={}; Method={}", origin.get(), acrMethod.get());
                return unauthorized(req);
            }
        }
        return Optional.absent();
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        AcceptedCORS cors = (AcceptedCORS) match.getOtherParams().get("cors");
        resp.setHeader("Access-Control-Allow-Origin", cors.getOrigin());
        if (!cors.getHeaders().isEmpty()) {
            resp.setHeader("Access-Control-Allow-Headers", Joiner.on(", ").join(cors.getHeaders()));
        }
        if (!cors.getMethods().isEmpty()) {
            resp.setHeader("Access-Control-Allow-Methods", Joiner.on(", ").join(cors.getMethods()));
        }
        if (cors.getAllowCredentials().or(false)) {
            resp.setHeader("Access-Control-Allow-Credentials", "true");
        }
        resp.setHeader("Access-Control-Max-Age", String.valueOf(cors.getMaxAge()));
    }

    public String toString() {
        return "CORSRoute";
    }
}
