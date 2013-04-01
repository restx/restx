package restx;

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
        public static final String RECORDING = "recording";
    }

    private final String mode;
    private final RouteLifecycleListener lifecycleListener;
    private final ImmutableList<RestxRouteMatch> matches;
    private final UnmodifiableIterator<RestxRouteMatch> matchesIterator;


    public RestxContext(String mode, RouteLifecycleListener lifecycleListener,
                        ImmutableList<RestxRouteMatch> matches) {
        this.mode = mode;
        this.lifecycleListener = lifecycleListener;
        this.matches = matches;
        this.matchesIterator = matches.iterator();
    }

    public RestxContext(String mode, RouteLifecycleListener lifecycleListener, ImmutableList<RestxRouteMatch> matches,
                        UnmodifiableIterator<RestxRouteMatch> matchesIterator) {
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

    public RestxRouteMatch nextHandlerMatch() {
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
            public void onRouteMatch(RestxRoute source) {
                lifecycleListener.onRouteMatch(source);
                listener.onRouteMatch(source);
            }

            @Override
            public void onBeforeWriteContent(RestxRoute source) {
                lifecycleListener.onBeforeWriteContent(source);
                listener.onBeforeWriteContent(source);
            }
        }, matches, matchesIterator);
    }

}
