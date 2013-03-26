package restx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import restx.specs.SpecRecorder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 3:29 PM
 */
public class RestxContext {
    public static class Modes {
        public static final String PROD = "prod";
        public static final String DEV = "dev";
        public static final String RECORDING = SpecRecorder.RECORDING;
    }

    private final String mode;
    private final RouteLifecycleListener lifecycleListener;
    private final List<RestxRoute> routes = Lists.newLinkedList();

    public RestxContext(String mode, RouteLifecycleListener lifecycleListener) {
        this.mode = mode;
        this.lifecycleListener = lifecycleListener;
    }

    public RestxContext(String mode, RouteLifecycleListener lifecycleListener, Collection<RestxRoute> routes) {
        this(mode, lifecycleListener);
        this.routes.addAll(routes);
    }

    public String getMode() {
        return mode;
    }

    public RouteLifecycleListener getLifecycleListener() {
        return lifecycleListener;
    }

    public boolean proceed(RestxRequest req, RestxResponse resp) throws IOException {
        while (!routes.isEmpty()) {
            RestxRoute route = routes.get(0);
            routes.remove(0);
            if (route.route(req, resp, this)) {
                return true;
            }
        }
        return false;
    }

    public RestxContext withRoutes(ImmutableList<RestxRoute> routes) {
        return new RestxContext(mode, lifecycleListener, routes);
    }

    public RestxContext withListener(final RouteLifecycleListener listener) {
        return new RestxContext(mode, new RouteLifecycleListener() {
            @Override
            public void onRouteMatch(RestxRoute source) {
                lifecycleListener.onRouteMatch(source);
                listener.onRouteMatch(source);
            }

            @Override
            public void onBeforeWriteContent(RestxRoute source) {
                lifecycleListener.onBeforeWriteContent(source);
                listener.onBeforeWriteContent(source);
            }
        }, routes);
    }

}
