package restx;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 7:53 AM
 */
public interface RestxRouteMatcher {
    Optional<RestxRouteMatch> match(String method, String path);
}
