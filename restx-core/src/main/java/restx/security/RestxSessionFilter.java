package restx.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.common.Crypto;
import restx.factory.Name;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 8:59 PM
 */
public class RestxSessionFilter implements RestxFilter {
    public static final Name<RestxSessionFilter> NAME = Name.of(RestxSessionFilter.class, "RestxSessionFilter");

    private static final String EXPIRES = "_expires";

    private final Logger logger = LoggerFactory.getLogger(RestxSessionFilter.class);

    private final RestxSession.Definition sessionDefinition;
    private final ObjectMapper mapper;
    private final SignatureKey signatureKey;
    private final RestxSessionCookieDescriptor restxSessionCookieDescriptor;
    private final RestxSession emptySession;

    RestxSessionFilter(RestxSession.Definition sessionDefinition, ObjectMapper mapper, SignatureKey signatureKey, RestxSessionCookieDescriptor restxSessionCookieDescriptor) {
        this.sessionDefinition = sessionDefinition;
        this.mapper = mapper;
        this.signatureKey = signatureKey;
        this.restxSessionCookieDescriptor = restxSessionCookieDescriptor;
        this.emptySession = new RestxSession(sessionDefinition, ImmutableMap.<String,String>of(),
                Optional.<RestxPrincipal>absent(), Duration.ZERO);
    }

    @Override
    public Optional<RestxRouteMatch> match(RestxRequest req) {
        return Optional.of(new RestxRouteMatch(this, "*", req.getRestxPath()));
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, final RestxResponse resp, RestxContext ctx) throws IOException {
        final RestxSession session = buildContextFromRequest(req);
        if (RestxContext.Modes.RECORDING.equals(ctx.getMode())) {
            // we clean up caches in recording mode so that each request records the cache loading
            // Note: having this piece of code here is not a very nice isolation of responsibilities
            // we could put it in a separate filter, but then it's not easy to be sure it's called right after this
            // filter. Until such a feature is introduced, the easy solution to put it here is used.
            session.cleanUpCaches();
        }
        RestxSession.setCurrent(session);
        try {
            RouteLifecycleListener lifecycleListener = new RouteLifecycleListener() {
                @Override
                public void onRouteMatch(RestxRoute source) {
                }

                @Override
                public void onBeforeWriteContent(RestxRoute source) {
                    RestxSession newSession = RestxSession.current();
                    if (newSession != session) {
                        updateSessionInClient(resp, newSession);
                    }
                }
            };
            RestxRouteMatch next = ctx.nextHandlerMatch();
            next.getHandler().handle(next, req, resp, ctx.withListener(lifecycleListener));
        } finally {
            RestxSession.setCurrent(null);
        }
    }

    public RestxSession buildContextFromRequest(RestxRequest req) throws IOException {
        String restxSessionCookieName = restxSessionCookieDescriptor.getCookieName();
        String cookie = req.getCookieValue(restxSessionCookieName, "");
        if (cookie.trim().isEmpty()) {
            return emptySession;
        } else {
            String sig = req.getCookieValue(restxSessionCookieDescriptor.getCookieSignatureName(), "");
            if (!Crypto.sign(cookie, signatureKey.getKey()).equals(sig)) {
                logger.warn("invalid restx session signature. session was: {}. Ignoring session cookie.", cookie);
                return emptySession;
            }
            Map entries = mapper.readValue(cookie, Map.class);
            DateTime expires = DateTime.parse((String) entries.remove(EXPIRES));
            if (expires.isBeforeNow()) {
                return emptySession;
            }

            Duration expiration = req.isPersistentCookie(restxSessionCookieName) ? new Duration(DateTime.now(), expires) : Duration.ZERO;
            ImmutableMap valueidsByKey = ImmutableMap.copyOf(entries);
            String principalName = (String) valueidsByKey.get(RestxPrincipal.SESSION_DEF_KEY);
            Optional<RestxPrincipal> principalOptional = RestxSession.getValue(
                    sessionDefinition, RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, principalName);
            if (principalOptional.isPresent() && principalOptional.get().getPrincipalRoles().contains("restx-admin")) {
                Optional<String> su = req.getHeader("RestxSu");
                if (su.isPresent() && !Strings.isNullOrEmpty(su.get())) {
                    try {
                        entries.putAll(mapper.readValue(su.get(), Map.class));
                        valueidsByKey = ImmutableMap.copyOf(entries);
                        principalName = (String) valueidsByKey.get(RestxPrincipal.SESSION_DEF_KEY);
                        principalOptional = RestxSession.getValue(
                                sessionDefinition, RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, principalName);
                        logger.info("restx-admin sudoing request with {}", su.get());
                    } catch (Exception e) {
                        logger.warn("restx-admin tried sudoing request with {}, but it failed: {}", su.get(), e.toString());
                        throw new WebException(HttpStatus.BAD_REQUEST, "invalid su session '" + su.get() + "': " + e.toString());
                    }
                }
            }
            return new RestxSession(sessionDefinition, valueidsByKey, principalOptional, expiration);
        }
    }

    private void updateSessionInClient(RestxResponse resp, RestxSession session) {
        ImmutableMap<String, String> cookiesMap = toCookiesMap(session);
        if (cookiesMap.isEmpty()) {
            resp.clearCookie(restxSessionCookieDescriptor.getCookieName());
            resp.clearCookie(restxSessionCookieDescriptor.getCookieSignatureName());
        } else {
            for (Map.Entry<String, String> cookie : cookiesMap.entrySet()) {
                resp.addCookie(cookie.getKey(), cookie.getValue(), session.getExpires());
            }
        }
    }

    public ImmutableMap<String, String> toCookiesMap(RestxSession session) {
        try {
            ImmutableMap<String, String> sessionMap = session.valueidsByKeyMap();
            if (sessionMap.isEmpty()) {
                return ImmutableMap.of();
            } else {
                HashMap<String,String> map = Maps.newHashMap(sessionMap);
                map.put(EXPIRES, DateTime.now().plusDays(30).toString());
                String sessionJson = mapper.writeValueAsString(map);
                return ImmutableMap.of(restxSessionCookieDescriptor.getCookieName(), sessionJson,
                        restxSessionCookieDescriptor.getCookieSignatureName(), Crypto.sign(sessionJson, signatureKey.getKey()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return "RestxSessionFilter";
    }
}
