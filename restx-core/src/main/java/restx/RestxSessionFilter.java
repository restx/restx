package restx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import restx.common.Crypto;
import restx.factory.*;
import restx.jackson.FrontObjectMapperFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 8:59 PM
 */
public class RestxSessionFilter implements RestxRoute {
    public static final Name<RestxSessionFilter> NAME = Name.of(RestxSessionFilter.class, "RestxSessionFilter");

    private static final String RESTX_SESSION_SIGNATURE = "RestxSessionSignature";
    private static final String RESTX_SESSION = "RestxSession";
    private static final String EXPIRES = "_expires";

    private final RestxSession.Definition sessionDefinition;
    private final ObjectMapper mapper;
    private final byte[] signatureKey;

    private RestxSessionFilter(RestxSession.Definition sessionDefinition, ObjectMapper mapper, byte[] signatureKey) {
        this.sessionDefinition = sessionDefinition;
        this.mapper = mapper;
        this.signatureKey = signatureKey;
    }

    @Override
    public boolean route(RestxRequest req, final RestxResponse resp, RestxContext ctx) throws IOException {
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
            return ctx.withListener(lifecycleListener).proceed(req, resp);
        } finally {
            RestxSession.setCurrent(null);
        }
    }

    private RestxSession buildContextFromRequest(RestxRequest req) throws IOException {
        String cookie = req.getCookieValue(RESTX_SESSION, "");
        if (cookie.trim().isEmpty()) {
            return new RestxSession(sessionDefinition, ImmutableMap.<String,String>of(), Duration.ZERO);
        } else {
            String sig = req.getCookieValue(RESTX_SESSION_SIGNATURE, "");
            if (!Crypto.sign(cookie, signatureKey).equals(sig)) {
                throw new IllegalArgumentException("invalid restx session signature");
            }
            Map entries = mapper.readValue(cookie, Map.class);
            DateTime expires = DateTime.parse((String) entries.remove(EXPIRES));
            if (expires.isBeforeNow()) {
                return new RestxSession(sessionDefinition, ImmutableMap.<String,String>of(), Duration.ZERO);
            }

            Duration expiration = req.isPersistentCookie(RESTX_SESSION) ? new Duration(DateTime.now(), expires) : Duration.ZERO;
            return new RestxSession(sessionDefinition, ImmutableMap.copyOf(entries), expiration);
        }
    }

    private void updateSessionInClient(RestxResponse resp, RestxSession session) {
        try {
            ImmutableMap<String, String> sessionMap = session.valueidsByKeyMap();
            if (sessionMap.isEmpty()) {
                resp.clearCookie(RESTX_SESSION);
                resp.clearCookie(RESTX_SESSION_SIGNATURE);
            } else {
                HashMap<String,String> map = Maps.newHashMap(sessionMap);
                map.put(EXPIRES, DateTime.now().plusDays(30).toString());
                String sessionJson = mapper.writeValueAsString(map);
                resp.addCookie(RESTX_SESSION, sessionJson, session.getExpires());
                resp.addCookie(RESTX_SESSION_SIGNATURE, Crypto.sign(sessionJson, signatureKey), session.getExpires());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public static class Machine extends SingleNameFactoryMachine<RestxSessionFilter> {

        public Machine() {
            super(-10, new StdMachineEngine<RestxSessionFilter>(NAME, BoundlessComponentBox.FACTORY) {
                private final Factory.Query<RestxSession.Definition.Entry> entries = Factory.Query.byClass(RestxSession.Definition.Entry.class);
                private final Factory.Query<ObjectMapper> mapper = Factory.Query.byName(FrontObjectMapperFactory.NAME);
                private final Factory.Query<SignatureKey> signatureKeyQuery = Factory.Query.byClass(SignatureKey.class);
                @Override
                public RestxSessionFilter doNewComponent(SatisfiedBOM satisfiedBOM) {
                    return new RestxSessionFilter(
                            new RestxSession.Definition(satisfiedBOM.getAsComponents(entries)),
                            satisfiedBOM.getOne(mapper).get().getComponent(),
                            satisfiedBOM.getOne(signatureKeyQuery)
                                    .or(new NamedComponent(
                                            Name.of(SignatureKey.class, "DefaultSignature"),
                                            new SignatureKey("this is the default signature key".getBytes())))
                                    .getComponent().getKey());
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return BillOfMaterials.of(entries, mapper, signatureKeyQuery);
                }
            });
        }
    }
}
