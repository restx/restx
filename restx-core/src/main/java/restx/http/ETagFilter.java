package restx.http;

import com.google.common.base.Optional;
import restx.AbstractRouteLifecycleListener;
import restx.RestxContext;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.RestxRouteFilter;
import restx.StdRestxRequestMatch;
import restx.WebException;
import restx.entity.StdEntityRoute;
import restx.factory.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * Date: 22/5/14
 * Time: 20:54
 */
@Component(priority = -90)
public class ETagFilter implements RestxRouteFilter {
    private final Collection<ETagProvider> providers;

    public ETagFilter(Collection<ETagProvider> providers) {
        this.providers = providers;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRoute route) {
        if (!(route instanceof StdEntityRoute)) {
            return Optional.absent();
        }
        StdEntityRoute stdEntityRoute = (StdEntityRoute) route;
        if (!(stdEntityRoute.getEntityResponseType() instanceof Class)) {
            return Optional.absent();
        }

        Class<?> clazz = (Class<?>) stdEntityRoute.getEntityResponseType();

        for (ETagProvider<?> provider : providers) {
            if (provider.getEntityType().isAssignableFrom(clazz)) {
                return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("/*"), new ETagHandler<>(provider)));
            }
        }

        return Optional.absent();
    }

    private static class ETagHandler<T> implements RestxHandler {
        private final ETagProvider<T> provider;

        public ETagHandler(ETagProvider<T> provider) {
            this.provider = provider;
        }

        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
            ctx.nextHandlerMatch().handle(req, resp, ctx.withListener(new AbstractRouteLifecycleListener() {
                @Override
                @SuppressWarnings("unchecked")
                public void onEntityOutput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input, Optional<?> output) {
                    if (output.isPresent()) {
                        provider.provideETagFor((T) output.get()).handleIn(req, resp);
                    }
                }
            }));
        }
    }
}
