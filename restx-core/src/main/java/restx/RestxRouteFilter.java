package restx;

import com.google.common.base.Optional;

/**
 * A filter that can determine if it must be applied or not at the route level.
 *
 * Route filters are more efficient during routing that simple RestxFilter, because
 * determining if they must be applied or not is not when wiring the routing, which in PROD mode occurs
 * only once at startup time.
 */
public interface RestxRouteFilter {
    Optional<RestxHandlerMatch> match(RestxRoute route);
}
