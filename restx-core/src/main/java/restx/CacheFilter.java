package restx;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import restx.factory.Component;

import java.io.IOException;

/**
 * Date: 1/12/13
 * Time: 14:55
 */
@Component(priority = 1000)
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
                                resp.setHeader("Cache-Control", "no-cache");
                            }
                        }));
                    }
                }));
    }
}
