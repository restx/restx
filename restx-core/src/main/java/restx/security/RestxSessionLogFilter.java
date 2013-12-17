package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.slf4j.MDC;
import restx.*;
import restx.factory.Component;

import java.io.IOException;

/**
 * This filter is responsible for:
 * - setting authenticated principal name in MDC.
 * - touching sessions, used in sessions admin console to get some details on client sessions.
 *
 * It is set at priority -110, authentication filters should be set between priority -200 (RestxSessionCookieFilter)
 * and -110 to be logged.
 */
@Component(priority = -110)
public class RestxSessionLogFilter implements RestxFilter, RestxHandler {
    private final Sessions sessions;

    public RestxSessionLogFilter(Sessions sessions) {
        this.sessions = sessions;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        return Optional.of(new RestxHandlerMatch(
                new StdRestxRequestMatch("*", req.getRestxPath()),
                this));
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        RestxSession session = RestxSession.current();
        ImmutableMap<String, String> metadata = prepareSessionStatsMetadata(req, session);
        if (session.getPrincipal().isPresent()) {
            String name = session.getPrincipal().get().getName();
            sessions.touch(name, metadata);
            MDC.put("principal", name);
        } else {
            sessions.touch("anonymous@" + req.getClientAddress(), metadata);
        }
        ctx.nextHandlerMatch().handle(req, resp, ctx);
    }

    /**
     * Prepares the metadata to be used for session stats monitoring.
     *
     * If you override this method, make sure to include the map built by the default implementation if you want
     * the monitor admin session view to work properly, unless you override it too.
     *
     * @param req the request for which metadata should be prepared
     * @param session the session for which metadata should be prepared
     * @return the prepared metadata
     */
    protected ImmutableMap<String, String> prepareSessionStatsMetadata(RestxRequest req, RestxSession session) {
        return ImmutableMap.of(
                "clientAddress", req.getClientAddress(),
                "userAgent", req.getHeader("User-Agent").or("Unknown"));
    }
}
