package restx.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import restx.AbstractRouteLifecycleListener;
import restx.RestxContext;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.RestxRouteFilter;
import restx.RouteLifecycleListener;
import restx.StdRestxRequestMatch;
import restx.WebException;
import restx.factory.Component;
import restx.factory.Name;
import restx.http.HttpStatus;
import restx.jackson.FrontObjectMapperFactory;

/**
 * This filter is used to store and get a RestxSession in Cookies (one for data, one for signature).
 */
@Component(priority = -200)
public class RestxSessionCookieFilter implements RestxRouteFilter, RestxHandler {
    public static final Name<RestxSessionCookieFilter> NAME = Name.of(RestxSessionCookieFilter.class, "RestxSessionCookieFilter");
	public static final String COOKIE_SIGNER_NAME = "CookieSigner";

    private static final String EXPIRES = "_expires";

    private final static Logger logger = LoggerFactory.getLogger(RestxSessionCookieFilter.class);

    private final RestxSession.Definition sessionDefinition;
    private final ObjectMapper mapper;
	private final Signer signer;
	private final RestxSessionCookieDescriptor restxSessionCookieDescriptor;
    private final RestxSession emptySession;

	public RestxSessionCookieFilter(
			RestxSession.Definition sessionDefinition,
			@Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper,
			@Named(COOKIE_SIGNER_NAME) Signer signer,
			RestxSessionCookieDescriptor restxSessionCookieDescriptor) {

		this.sessionDefinition = sessionDefinition;
		this.mapper = mapper;
		this.signer = signer;
		this.restxSessionCookieDescriptor = restxSessionCookieDescriptor;
		this.emptySession = new RestxSession(sessionDefinition, ImmutableMap.<String, String>of(),
				Optional.<RestxPrincipal>absent(), Duration.ZERO);
	}

    @Override
    public Optional<RestxHandlerMatch> match(RestxRoute route) {
        return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("/*"), this));
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, final RestxResponse resp, RestxContext ctx) throws IOException {
        final RestxSession session = buildContextFromRequest(req);
        if (RestxContext.Modes.RECORDING.equals(ctx.getMode())) {
            // we invalidate caches in recording mode so that each request records the cache loading
            // Note: having this piece of code here is not a very nice isolation of responsibilities
            // we could put it in a separate filter, but then it's not easy to be sure it's called right after this
            // filter. Until such a feature is introduced, the easy solution to put it here is used.
            sessionDefinition.invalidateAllCaches();
        }
        RestxSession.setCurrent(session);
        try {
            RouteLifecycleListener lifecycleListener = new AbstractRouteLifecycleListener() {
                @Override
                public void onBeforeWriteContent(RestxRequest req, RestxResponse resp) {
                    RestxSession newSession = RestxSession.current();
                    if (newSession != session) {
                        updateSessionInClient(resp, newSession);
                    }
                }
            };
            ctx.nextHandlerMatch().handle(req, resp, ctx.withListener(lifecycleListener));
        } finally {
            RestxSession.setCurrent(null);
            // we don't remove the MDC principal here, we want to keep it until the end of the request
        }
    }

    public RestxSession buildContextFromRequest(RestxRequest req) throws IOException {
        String restxSessionCookieName = restxSessionCookieDescriptor.getCookieName();
        String cookie = req.getCookieValue(restxSessionCookieName).or("");
        if (cookie.trim().isEmpty()) {
            return emptySession;
        } else {
            String sig = req.getCookieValue(restxSessionCookieDescriptor.getCookieSignatureName()).or("");
			if (!signer.verify(cookie, sig)) {
				logger.warn("invalid restx session signature. session was: {}. Ignoring session cookie.", cookie);
                return emptySession;
            }
            Map<String, String> entries = readEntries(cookie);
            DateTime expires = DateTime.parse(entries.remove(EXPIRES));
            if (expires.isBeforeNow()) {
                return emptySession;
            }

            Duration expiration = req.isPersistentCookie(restxSessionCookieName) ? new Duration(DateTime.now(), expires) : Duration.ZERO;
            ImmutableMap<String, String> valueidsByKey = ImmutableMap.copyOf(entries);
            String principalName = valueidsByKey.get(RestxPrincipal.SESSION_DEF_KEY);
            Optional<RestxPrincipal> principalOptional = RestxSession.getValue(
                    sessionDefinition, RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, principalName);
            if (principalOptional.isPresent()
                    && Permissions.hasRole("restx-admin").has(principalOptional.get(), null).isPresent()) {
                Optional<String> su = req.getHeader("RestxSu");
                if (su.isPresent() && !Strings.isNullOrEmpty(su.get())) {
                    try {
                        entries.putAll(readEntries(su.get()));
                        valueidsByKey = ImmutableMap.copyOf(entries);
                        principalName = valueidsByKey.get(RestxPrincipal.SESSION_DEF_KEY);
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

    @SuppressWarnings("unchecked")
    protected Map<String, String> readEntries(String cookie) throws IOException {
        return mapper.readValue(cookie, Map.class);
    }

    private void updateSessionInClient(RestxResponse resp, RestxSession session) {
        ImmutableMap<String, String> cookiesMap = toCookiesMap(session);
        if (cookiesMap.isEmpty()) {
            resp.clearCookie(restxSessionCookieDescriptor.getCookieName());
            resp.clearCookie(restxSessionCookieDescriptor.getCookieSignatureName());
        } else {
            for (Map.Entry<String, String> cookie : cookiesMap.entrySet()) {
                logger.debug("setting cookie: {} {}", cookie.getKey(), cookie.getValue());
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
						restxSessionCookieDescriptor.getCookieSignatureName(), signer.sign(sessionJson));
			}
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return "RestxSessionCookieFilter";
    }
}
