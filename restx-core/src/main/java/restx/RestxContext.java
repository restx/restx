package restx;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 3:29 PM
 */
public class RestxContext {
    public static class Modes {
        public static final String PROD = "prod";
        public static final String DEV = "dev";
        public static final String TEST = "test";
        public static final String INFINIREST = "infinirest";
        public static final String RECORDING = "recording";
    }

    private final String mode;
    private final RouteLifecycleListener lifecycleListener;
    private final ImmutableList<RestxHandlerMatch> matches;
    private final UnmodifiableIterator<RestxHandlerMatch> matchesIterator;


    public RestxContext(String mode, RouteLifecycleListener lifecycleListener,
                        ImmutableList<RestxHandlerMatch> matches) {
        this.mode = mode;
        this.lifecycleListener = lifecycleListener;
        this.matches = matches;
        this.matchesIterator = matches.iterator();
    }

    public RestxContext(String mode, RouteLifecycleListener lifecycleListener, ImmutableList<RestxHandlerMatch> matches,
                        UnmodifiableIterator<RestxHandlerMatch> matchesIterator) {
        this.mode = mode;
        this.lifecycleListener = lifecycleListener;
        this.matches = matches;
        this.matchesIterator = matchesIterator;
    }

    public String getMode() {
        return mode;
    }

    public RouteLifecycleListener getLifecycleListener() {
        return lifecycleListener;
    }

    public RestxHandlerMatch nextHandlerMatch() {
        if (matchesIterator.hasNext()) {
            return matchesIterator.next();
        }
        throw new IllegalStateException(
                "no next handler match. " +
                "this is probably caused either by a filter calling next() twice or more, " +
                "or by a route calling next(). " +
                "list of handler matches: " + matches);
    }

    public RestxContext withListener(final RouteLifecycleListener listener) {
        return new RestxContext(mode, new RouteLifecycleListener() {
            @Override
            public void onRouteMatch(RestxRoute source, RestxRequest req, RestxResponse resp) {
                lifecycleListener.onRouteMatch(source, req, resp);
                listener.onRouteMatch(source, req, resp);
            }

            @Override
            public void onBeforeWriteContent(RestxRequest req, RestxResponse resp) {
                lifecycleListener.onBeforeWriteContent(req, resp);
                listener.onBeforeWriteContent(req, resp);
            }

            @Override
            public void onAfterWriteContent(RestxRequest req, RestxResponse resp) {
                lifecycleListener.onAfterWriteContent(req, resp);
                listener.onAfterWriteContent(req, resp);
            }

            @Override
            public void onEntityInput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input) {
                lifecycleListener.onEntityInput(route, req, resp, input);
                listener.onEntityInput(route, req, resp, input);
            }

            @Override
            public void onEntityOutput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input, Optional<?> output) {
                lifecycleListener.onEntityOutput(route, req, resp, input, output);
                listener.onEntityOutput(route, req, resp, input, output);
            }
        }, matches, matchesIterator);
    }

}
