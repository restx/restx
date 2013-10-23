package restx;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 10:12 PM
 */
public interface RouteLifecycleListener {
    public static final RouteLifecycleListener DEAF = new AbstractRouteLifecycleListener() {};

    void onRouteMatch(RestxRoute route, RestxRequest req, RestxResponse resp);
    void onBeforeWriteContent(RestxRequest req, RestxResponse resp);
    void onAfterWriteContent(RestxRequest req, RestxResponse resp);
}
