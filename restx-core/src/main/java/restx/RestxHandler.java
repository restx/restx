package restx;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 9:30 PM
 */
public interface RestxHandler {
    void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException;
}
