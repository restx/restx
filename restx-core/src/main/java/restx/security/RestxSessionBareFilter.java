package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.joda.time.Duration;
import restx.*;
import restx.factory.Component;

import java.io.IOException;

/**
 * Sets an empty RestxSession in current thread if none has been assigned yet.
 * Used only when RestxSessionCookieFilter is deactivated.
 */
@Component(priority = -199)
public class RestxSessionBareFilter implements RestxFilter, RestxHandler {
    private final RestxSession.Definition definition;

    public RestxSessionBareFilter(RestxSession.Definition definition) {
        this.definition = definition;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        return Optional.of(new RestxHandlerMatch(
                new StdRestxRequestMatch("*", req.getRestxPath()),
                this));
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        boolean hasSetSession = false;
        if (RestxSession.current() == null) {
            RestxSession.setCurrent(new RestxSession(
                    definition,
                    ImmutableMap.<String, String>of(),
                    Optional.<RestxPrincipal>absent(),
                    Duration.ZERO));
            hasSetSession = true;
        }
        try {
            ctx.nextHandlerMatch().handle(req, resp, ctx);
        } finally {
            if (hasSetSession) {
                RestxSession.setCurrent(null);
            }
        }
    }
}
