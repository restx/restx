package restx;

import com.google.common.base.Optional;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 9:30 PM
 */
public interface RestxHandler {
    Optional<RestxRouteMatch> match(RestxRequest req);
    void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException;
}
