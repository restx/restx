package restx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.Crypto;
import restx.factory.Factory;
import restx.jackson.FrontObjectMapperFactory;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 2/6/13
 * Time: 9:53 PM
 */
public class RestxMainRouter {
    private static final String RESTX_CTX_SIGNATURE = "RestxCtxSignature";
    private static final String RESTX_CTX = "RestxCtx";

    private final Logger logger = LoggerFactory.getLogger(RestxMainRouter.class);

    private RestxRouter mainRouter;
    private RestxContext.Definition ctxDefinition;
    private ObjectMapper mapper;
    private byte[] signatureKey;

    public void init() {
        if (getLoadFactoryMode().equals("onstartup")) {
            loadFactory("");
        }
    }

    private void loadFactory(String context) {
        Factory factory = Factory.builder()
                .addFromServiceLoader()
                .addLocalMachines(Factory.LocalMachines.threadLocal())
                .addLocalMachines(Factory.LocalMachines.contextLocal(context))
                .build();

        logger.debug("restx factory ready: {}", factory);

        ctxDefinition = new RestxContext.Definition(factory.getComponents(RestxContext.Definition.Entry.class));
        mapper = factory.getNamedComponent(FrontObjectMapperFactory.NAME).get().getComponent();
        signatureKey = Iterables.getFirst(factory.getComponents(SignatureKey.class),
                new SignatureKey("this is the default signature key".getBytes())).getKey();

        mainRouter = new RestxRouter("MainRouter", ImmutableList.copyOf(factory.getComponents(RestxRoute.class)));
        logger.info("restx main router servlet ready: " + mainRouter);
    }


    public void route(String contextName, RestxRequest restxRequest, final RestxResponse restxResponse) throws IOException {
        logger.info("incoming request {}", restxRequest);
        if (getLoadFactoryMode().equals("onrequest")) {
            loadFactory(contextName);
        }

        final RestxContext ctx = buildContextFromRequest(restxRequest);
        RestxContext.setCurrent(ctx);
        try {
            if (!mainRouter.route(restxRequest, restxResponse, new RouteLifecycleListener() {
                @Override
                public void onRouteMatch(RestxRoute source) {
                }

                @Override
                public void onBeforeWriteContent(RestxRoute source) {
                    RestxContext newCtx = RestxContext.current();
                    if (newCtx != ctx) {
                        updateContextInClient(restxResponse, newCtx);
                    }
                }
            })) {
                String path = restxRequest.getRestxPath();
                String msg = String.format(
                        "no restx route found for %s %s\n" +
                        "routes:\n" +
                        "-----------------------------------\n" +
                        "%s\n" +
                        "-----------------------------------",
                        restxRequest.getHttpMethod(), path, mainRouter);
                restxResponse.setStatus(404);
                restxResponse.setContentType("text/plain");
                restxResponse.getWriter().print(msg);
                restxResponse.getWriter().close();
            }
        } catch (IllegalArgumentException ex) {
            logger.info("request raised IllegalArgumentException", ex);
            restxResponse.setStatus(400);
            restxResponse.setContentType("text/plain");
            restxResponse.getWriter().print(ex.getMessage());
            restxResponse.getWriter().close();
        } finally {
            RestxContext.setCurrent(null);
        }
    }

    private String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load", "onstartup");
    }

    private RestxContext buildContextFromRequest(RestxRequest req) throws IOException {
        String cookie = req.getCookieValue(RESTX_CTX, "");
        if (cookie.trim().isEmpty()) {
            return new RestxContext(ctxDefinition, ImmutableMap.<String,String>of());
        } else {
            String sig = req.getCookieValue(RESTX_CTX_SIGNATURE, "");
            if (!Crypto.sign(cookie, signatureKey).equals(sig)) {

            }
            return new RestxContext(ctxDefinition, ImmutableMap.copyOf(mapper.readValue(cookie, Map.class)));
        }
    }

    private void updateContextInClient(RestxResponse resp, RestxContext ctx) {
        try {
            String value = mapper.writeValueAsString(ctx.keysByNameMap());
            resp.addCookie(new Cookie(RESTX_CTX, value));
            resp.addCookie(new Cookie(RESTX_CTX_SIGNATURE, Crypto.sign(value, signatureKey)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
