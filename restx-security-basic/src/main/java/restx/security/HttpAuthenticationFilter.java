package restx.security;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.factory.Component;
import restx.http.HttpStatus;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Locale;

/**
 * Date: 16/12/13
 * Time: 22:29
 */
@Component(priority = -190)
public class HttpAuthenticationFilter implements RestxFilter {
    private static final Logger logger = LoggerFactory.getLogger(HttpAuthenticationFilter.class);

    private HttpBasicAuthHandler basicHandler;

    public HttpAuthenticationFilter(HttpBasicAuthHandler basicHandler) {
        this.basicHandler = basicHandler;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        Optional<String> authorization = req.getHeader("Authorization");
        if (authorization.isPresent()) {
            if (authorization.get().toLowerCase(Locale.ENGLISH).startsWith("basic ")) {
                return Optional.of(new RestxHandlerMatch(
                    new StdRestxRequestMatch("*", req.getRestxPath()),
                        basicHandler));
            } else {
                logger.warn("unsupported authentication type: " + authorization.get());
            }
        }
        return Optional.absent();
    }

    @Override
    public String toString() {
        return "HttpAuthenticationFilter";
    }

    @Component
    public static class HttpBasicAuthHandler implements RestxHandler {
        private final BasicPrincipalAuthenticator authenticator;

        public HttpBasicAuthHandler(BasicPrincipalAuthenticator authenticator) {
            this.authenticator = authenticator;
        }

        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
            String base64Pwd = req.getHeader("Authorization").get().substring("basic ".length());
            String auth = new String(BaseEncoding.base64().decode(base64Pwd), Charsets.UTF_8);
            int i = auth.indexOf(':');
            if (i < 0) {
                throw new WebException(HttpStatus.BAD_REQUEST,
                        "Invalid Basic Authentication. It must have the form <user>:<pwd>. It was: " + auth);
            }
            String u = auth.substring(0, i);
            String pwd = auth.substring(i + 1);

            Optional<? extends RestxPrincipal> principal = authenticator.authenticate(
                    u, pwd, ImmutableMap.<String, Object>of());

            if (principal.isPresent()) {
                logger.debug("http basic authenticated '{}'", principal.get().getName());
                RestxSession.current().authenticateAs(principal.get());
            } else {
                throw new WebException(HttpStatus.UNAUTHORIZED);
            }
            ctx.nextHandlerMatch().handle(req, resp, ctx);
        }

        @Override
        public String toString() {
            return "HttpBasicAuthHandler";
        }

    }
}
