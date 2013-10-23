package restx;

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
}
