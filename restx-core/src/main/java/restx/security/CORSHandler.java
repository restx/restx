package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.*;
import restx.http.HttpStatus;

import java.io.IOException;

/**
 * Date: 28/12/13
 * Time: 11:33
 */
public class CORSHandler {
    private static final RestxHandler UNAUTHORIZED_HANDLER = new RestxHandler() {
        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx)
                throws IOException {
            resp.setStatus(HttpStatus.FORBIDDEN);
        }
    };

    protected Optional<RestxHandlerMatch> unauthorized(RestxRequest req) {
        return Optional.of(
                new RestxHandlerMatch(new StdRestxRequestMatch("*", req.getRestxPath(),
                        ImmutableMap.<String, String>of(), ImmutableMap.<String, String>of()), UNAUTHORIZED_HANDLER));
    }
}
