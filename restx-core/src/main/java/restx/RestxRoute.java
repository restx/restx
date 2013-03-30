package restx;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:00 AM
 */
public interface RestxRoute {
    boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException;
}
