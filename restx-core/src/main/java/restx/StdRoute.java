package restx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import restx.description.DescribableRoute;
import restx.description.OperationDescription;
import restx.description.ResourceDescription;
import restx.jackson.Views;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdRoute implements RestxRoute, DescribableRoute {
    private final String name;
    private final RestxRouteMatcher matcher;
    protected final ObjectMapper mapper;

    public StdRoute(String name, ObjectMapper mapper, RestxRouteMatcher matcher) {
        this.name = checkNotNull(name);
        this.mapper = checkNotNull(mapper);
        this.matcher = checkNotNull(matcher);
    }

    @Override
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        String path = req.getRestxPath();
        Optional<RestxRouteMatch> match = matcher.match(req.getHttpMethod(), path);
        if (match.isPresent()) {
            ctx.getLifecycleListener().onRouteMatch(this);
            Optional<?> result = doRoute(req, match.get());
            if (result.isPresent()) {
                resp.setStatus(200);
                resp.setContentType("application/json");
                Object value = result.get();
                if (value instanceof Iterable) {
                    value = Lists.newArrayList((Iterable) value);
                }
                ctx.getLifecycleListener().onBeforeWriteContent(this);
                writeValue(mapper, resp.getWriter(), value);
            } else {
                resp.setStatus(404);
                resp.setContentType("text/plain");
                resp.getWriter().println("Route matched, but resource " + path + " not found.");
                resp.getWriter().println("Matched route: " + this);
                resp.getWriter().println("Path params: " + match.get().getPathParams());
            }
            return true;
        }
        return false;
    }

    protected void writeValue(ObjectMapper mapper, PrintWriter writer, Object value) throws IOException {
        getObjectWriter(mapper).writeValue(writer, value);
    }

    protected ObjectWriter getObjectWriter(ObjectMapper mapper) {
        return mapper.writerWithView(Views.Transient.class);
    }

    @Override
    public Collection<ResourceDescription> describe() {
        if (matcher instanceof StdRouteMatcher) {
            ResourceDescription description = new ResourceDescription();
            StdRouteMatcher stdRouteMatcher = (StdRouteMatcher) matcher;
            description.path = stdRouteMatcher.getPath();
            OperationDescription operation = new OperationDescription();
            operation.httpMethod = stdRouteMatcher.getMethod();
            operation.nickname = name.substring(name.lastIndexOf('#') + 1);
            describeOperation(operation);
            description.operations = Collections.singletonList(operation);
            return Collections.singleton(description);
        } else {
            return Collections.emptySet();
        }
    }

    // override to provide parameters, response and error codes description
    protected void describeOperation(OperationDescription operation) {
    }

    protected abstract Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException;

    @Override
    public String toString() {
        return matcher.toString() + " => " + name;
    }
}
