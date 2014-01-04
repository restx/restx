package restx;

import com.google.common.base.Optional;

/**
 * Date: 19/10/13
 * Time: 20:59
 */
public  abstract class AbstractRouteLifecycleListener implements RouteLifecycleListener {
    @Override
    public void onRouteMatch(RestxRoute route, RestxRequest req, RestxResponse resp) {
    }

    @Override
    public void onBeforeWriteContent(RestxRequest req, RestxResponse resp) {
    }

    @Override
    public void onAfterWriteContent(RestxRequest req, RestxResponse resp) {
    }

    @Override
    public void onEntityInput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input) {
    }

    @Override
    public void onEntityOutput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input, Optional<?> output) {
    }
}
