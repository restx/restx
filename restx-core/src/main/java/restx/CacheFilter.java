package restx;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import restx.factory.Component;

import java.io.IOException;

/**
 * This filter is used to set a Cache-Control no-cache header on responses.
 *
 * This disable caching by default on resources, the content of them being most of the time dynamic, it's
 * easier to have cache disabled by default.
 *
 * This can be configured in the settings with the cachedResources, which allow to set the restx paths on which this
 * filter should not be applied.
 *
 * Moreover, this filter won't set the Cache-Control header if it's already set, so it doesn't prevent other more
 * specific caching strategies.
 *
 * Finally, it's also possible to disable this filter as any other Restx component, by setting
 * restx.activation::restx.GzipFilter::CacheFilter=false
 */
@Component(priority = -100)
public class CacheFilter implements RestxFilter {
    private final ImmutableList<String> paths;

    public CacheFilter(AppSettings settings) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String s : Splitter.on(",").trimResults().omitEmptyStrings().split(settings.cachedResources())) {
            builder.add(s);
        }
        paths = builder.build();
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        for (String path : paths) {
            if (req.getRestxPath().startsWith(path)) {
                return Optional.absent();
            }
        }

        return Optional.of(
                new RestxHandlerMatch(new StdRestxRequestMatch("*", req.getRestxPath()),
                new RestxHandler() {
                    @Override
                    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx)
                            throws IOException {
                        ctx.nextHandlerMatch().handle(req, resp, ctx.withListener(new AbstractRouteLifecycleListener() {
                            @Override
                            public void onBeforeWriteContent(RestxRequest req, RestxResponse resp) {
                                if (!resp.getHeader("Cache-Control").isPresent()) {
                                    resp.setHeader("Cache-Control", "no-cache");
                                }
                            }
                        }));
                    }
                }));
    }
}
