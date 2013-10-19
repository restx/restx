package restx;

/**
 * Date: 19/10/13
 * Time: 20:59
 */
public  abstract class AbstractRouteLifecycleListener implements RouteLifecycleListener {
    @Override
    public void onRouteMatch(RestxRoute source) {
    }

    @Override
    public void onBeforeWriteContent(RestxRoute source) {
    }

    @Override
    public void onAfterWriteContent(RestxRoute source) {
    }
}
