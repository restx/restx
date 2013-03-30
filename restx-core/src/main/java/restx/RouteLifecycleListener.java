package restx;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 10:12 PM
 */
public interface RouteLifecycleListener {
    public static final RouteLifecycleListener DEAF = new RouteLifecycleListener() {
        @Override
        public void onRouteMatch(RestxRoute source) {
        }

        @Override
        public void onBeforeWriteContent(RestxRoute source) {
        }
    };

    void onRouteMatch(RestxRoute source);
    void onBeforeWriteContent(RestxRoute source);
}
