package restx.http;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import restx.*;
import restx.description.OperationDescription;
import restx.description.ResourceDescription;

import java.io.IOException;
import java.util.Collection;

public abstract class EntityRelatedFilter implements RestxRouteFilter {

    private final Predicate<StdRoute> routeMatcher;
    private final Predicate<ResourceDescription> resourceDescriptionMatcher;
    private final Predicate<OperationDescription> operationDescriptionMatcher;

    public EntityRelatedFilter(Predicate<StdRoute> routeMatcher, Predicate<ResourceDescription> resourceDescriptionMatcher, Predicate<OperationDescription> operationDescriptionMatcher) {
        this.routeMatcher = routeMatcher;
        this.resourceDescriptionMatcher = resourceDescriptionMatcher;
        this.operationDescriptionMatcher = operationDescriptionMatcher;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRoute route) {
        if(!(route instanceof StdRoute)) {
            return Optional.absent();
        }

        final StdRoute stdRoute = (StdRoute) route;

        Collection<ResourceDescription> resourceDescriptionColl = stdRoute.describe();
        if(resourceDescriptionColl.isEmpty()) {
            return Optional.absent();
        }

        final ResourceDescription resourceDescription = Iterables.getOnlyElement(resourceDescriptionColl);
        if(resourceDescription.operations == null || resourceDescription.operations.isEmpty()) {
            return Optional.absent();
        }

        final OperationDescription operationDescription = Iterables.getOnlyElement(resourceDescription.operations);
        if(!matches(stdRoute, resourceDescription, operationDescription)) {
            return Optional.absent();
        }

        return Optional.of(new RestxHandlerMatch(new StdRestxRequestMatch("/*"), new RestxHandler() {
            @Override
            public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, final RestxContext ctx) throws IOException {
                ctx.nextHandlerMatch().handle(req, resp, ctx.withListener(new AbstractRouteLifecycleListener() {

                    @Override
                    public void onBeforeWriteContent(RestxRequest req, RestxResponse resp) {
                        EntityRelatedFilter.this.onBeforeWriteContent(req, resp, resourceDescription, operationDescription);
                    }

                    @Override
                    public void onAfterWriteContent(RestxRequest req, RestxResponse resp) {
                        EntityRelatedFilter.this.onAfterWriteContent(req, resp, resourceDescription, operationDescription);
                    }

                    @Override
                    public void onEntityInput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input) {
                        EntityRelatedFilter.this.onEntityInput(stdRoute, req, resp, input, resourceDescription, operationDescription);
                    }

                    @Override
                    public void onEntityOutput(RestxRoute route, RestxRequest req, RestxResponse resp, Optional<?> input, Optional<?> output) {
                        EntityRelatedFilter.this.onEntityOutput(stdRoute, req, resp, input, output, resourceDescription, operationDescription);
                    }
                }));
            }
        }));
    }

    protected void onEntityInput(StdRoute stdRoute, RestxRequest req, RestxResponse resp, Optional<?> input,
                                 ResourceDescription resourceDescription, OperationDescription operationDescription) {
        // To be overwritten
    }

    protected void onBeforeWriteContent(RestxRequest req, RestxResponse resp,
                                        ResourceDescription resourceDescription, OperationDescription operationDescription) {
        // To be overwritten
    }

    protected void onEntityOutput(StdRoute stdRoute, RestxRequest req, RestxResponse resp,
                                  Optional<?> input, Optional<?> output,
                                  ResourceDescription resourceDescription, OperationDescription operationDescription) {
        // To be overwritten
    }

    protected void onAfterWriteContent(RestxRequest req, RestxResponse resp,
                                       ResourceDescription resourceDescription, OperationDescription operationDescription) {
        // To be overwritten
    }


    protected boolean matches(StdRoute route, ResourceDescription resourceDescription, OperationDescription operationDescription) {
        return this.routeMatcher.apply(route)
                && this.resourceDescriptionMatcher.apply(resourceDescription)
                && this.operationDescriptionMatcher.apply(operationDescription);
    }
}
