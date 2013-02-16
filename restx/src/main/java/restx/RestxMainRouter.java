package restx;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/16/13
 * Time: 3:56 PM
 */
public interface RestxMainRouter {
    void route(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException;
}
