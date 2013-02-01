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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 2:46 PM
 */
public class RestxMainRouterServlet extends HttpServlet {
    private static final String RESTX_CTX_SIGNATURE = "RestxCtxSignature";
    private static final String RESTX_CTX = "RestxCtx";
    private final Logger logger = LoggerFactory.getLogger(RestxMainRouterServlet.class);


    private RestxRouter mainRouter;
    private RestxContext.Definition ctxDefinition;
    private ObjectMapper mapper;
    private byte[] signatureKey;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

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

        ctxDefinition = new RestxContext.Definition(factory.getComponents(RestxContext.Definition.Entry.class));
        mapper = factory.getNamedComponent(FrontObjectMapperFactory.NAME).get().getComponent();
        signatureKey = Iterables.getFirst(factory.getComponents(SignatureKey.class),
                new SignatureKey("this is the default signature key".getBytes())).getKey();

        mainRouter = new RestxRouter("MainRouter", ImmutableList.copyOf(factory.getComponents(RestxRoute.class)));
        logger.info("restx main router servlet ready: " + mainRouter);
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        logger.info("incoming request {}", req);

        if (getLoadFactoryMode().equals("onrequest")) {
            loadFactory(getFactoryContextName(req.getServerPort()));
        }

        final RestxContext ctx = buildContextFromRequest(req);
        RestxContext.setCurrent(ctx);
        try {
            if (!mainRouter.route(req, resp, new RouteLifecycleListener() {
                @Override
                public void onRouteMatch(RestxRoute source) {
                }

                @Override
                public void onBeforeWriteContent(RestxRoute source) {
                    RestxContext newCtx = RestxContext.current();
                    if (newCtx != ctx) {
                        updateContextInClient(resp, newCtx);
                    }
                }
            })) {
                logger.info("no route found for {}\n" +
                        "routes:\n" +
                        "-----------------------------------\n" +
                        "{}\n" +
                        "-----------------------------------", req, mainRouter);
                resp.setStatus(404);
            }
        } finally {
            RestxContext.setCurrent(null);
        }
    }

    public static String getFactoryContextName(int port) {
        return String.format("RESTX@%s", port);
    }

    private String getLoadFactoryMode() {
        return System.getProperty("restx.factory.load", "onstartup");
    }

    private RestxContext buildContextFromRequest(HttpServletRequest req) throws IOException {
        String cookie = getCookieValue(req.getCookies(), RESTX_CTX, "");
        if (cookie.trim().isEmpty()) {
            return new RestxContext(ctxDefinition, ImmutableMap.<String,String>of());
        } else {
            String sig = getCookieValue(req.getCookies(), RESTX_CTX_SIGNATURE, "");
            if (!Crypto.sign(cookie, signatureKey).equals(sig)) {

            }
            return new RestxContext(ctxDefinition, ImmutableMap.copyOf(mapper.readValue(cookie, Map.class)));
        }
    }

    private void updateContextInClient(HttpServletResponse resp, RestxContext ctx) {
        try {
            String value = mapper.writeValueAsString(ctx.keysByNameMap());
            resp.addCookie(new Cookie(RESTX_CTX, value));
            resp.addCookie(new Cookie(RESTX_CTX_SIGNATURE, Crypto.sign(value, signatureKey)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCookieValue(Cookie[] cookies,
                                          String cookieName,
                                          String defaultValue) {
        if (cookies == null) {
            return defaultValue;
        }
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookieName.equals(cookie.getName()))
                return cookie.getValue();
        }
        return defaultValue;
    }
}
