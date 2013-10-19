package restx;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 9:30 PM
 */
public interface RestxHandlerMatcher {
    Optional<? extends RestxRouteMatch> match(RestxRequest req);
}
