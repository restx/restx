package samplest.core;

import com.google.common.base.Optional;
import restx.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 4/1/14
 * Time: 09:27
 */
public class LifecycleListenerFilter implements RestxFilter, RestxHandler, RouteLifecycleListener {
    List<RestxRoute> matched = new ArrayList<>();
    List<Optional<?>> inputs = new ArrayList<>();
    List<Optional<?>> outputs = new ArrayList<>();
    int beforeWriteContentCount;
    int afterWriteContentCount;

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        if (req.getRestxPath().startsWith("/core/hello")) {
            return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch(req.getRestxPath()), this));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        ctx.nextHandlerMatch().handle(req, resp, ctx.withListener(this));
    }

    @Override
    public void onRouteMatch(RestxRoute route, RestxRequest req, RestxResponse resp) {
        matched.add(route);
    }

    @Override
    public void onEntityInput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input) {
        inputs.add(input);
    }

    @Override
    public void onEntityOutput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input, Optional<?> output) {
        outputs.add(output);
    }

    @Override
    public void onBeforeWriteContent(RestxRequest req, RestxResponse resp) {
        beforeWriteContentCount++;
    }

    @Override
    public void onAfterWriteContent(RestxRequest req, RestxResponse resp) {
        afterWriteContentCount++;
    }
}
