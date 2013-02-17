package restx;

import com.google.common.base.Optional;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 2/17/13
 * Time: 1:06 PM
 */
public interface MatchedEntityRoute {
    Optional<?> route(RestxRequest restxRequest, RestxRouteMatch match) throws IOException;
}
