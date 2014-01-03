package restx;

import com.google.common.base.Optional;
import restx.entity.StdEntityRoute;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 10:12 PM
 */
public interface RouteLifecycleListener {
    public static final RouteLifecycleListener DEAF = new AbstractRouteLifecycleListener() {};

    void onRouteMatch(RestxRoute route, RestxRequest req, RestxResponse resp);
    void onEntityInput(RestxRoute route, RestxRequest req, RestxResponse resp, Object input);
    void onEntityOutput(RestxRoute route, RestxRequest req, RestxResponse resp, Object input, Optional<?> output);
    void onBeforeWriteContent(RestxRequest req, RestxResponse resp);
    void onAfterWriteContent(RestxRequest req, RestxResponse resp);
}
