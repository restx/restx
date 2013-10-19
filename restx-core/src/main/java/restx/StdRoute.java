package restx;

import com.google.common.base.Optional;
import restx.description.DescribableRoute;
import restx.description.OperationDescription;
import restx.description.ResourceDescription;
import restx.http.HttpStatus;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdRoute implements RestxRoute, DescribableRoute, RestxHandler {
    private final String name;
    private final RestxRouteMatcher matcher;
    private final HttpStatus successStatus;

    public StdRoute(String name, RestxRouteMatcher matcher) {
        this(name, matcher, HttpStatus.OK);
    }

    public StdRoute(String name, RestxRouteMatcher matcher, HttpStatus successStatus) {
        this.name = checkNotNull(name);
        this.matcher = checkNotNull(matcher);
        this.successStatus = checkNotNull(successStatus);
    }

    @Override
    public Optional<? extends RestxRouteMatch> match(RestxRequest req) {
        String path = req.getRestxPath();
        return matcher.match(this, req.getHttpMethod(), path);
    }

    @Override
    public Collection<ResourceDescription> describe() {
        if (matcher instanceof StdRouteMatcher) {
            ResourceDescription description = new ResourceDescription();
            StdRouteMatcher stdRouteMatcher = (StdRouteMatcher) matcher;
            description.path = stdRouteMatcher.getPathPattern();
            OperationDescription operation = new OperationDescription();
            operation.httpMethod = stdRouteMatcher.getMethod();
            operation.nickname = name.substring(name.lastIndexOf('#') + 1);
            operation.successStatus = getSuccessStatus().createDescriptor();
            describeOperation(operation);
            description.operations = Collections.singletonList(operation);
            return Collections.singleton(description);
        } else {
            return Collections.emptySet();
        }
    }

    public HttpStatus getSuccessStatus() {
        return successStatus;
    }

    // override to provide parameters, response and error codes description
    protected void describeOperation(OperationDescription operation) {
    }

    @Override
    public String toString() {
        return matcher.toString() + " => " + name;
    }

    protected void notFound(RestxRouteMatch match, RestxResponse resp) throws IOException {
        resp.setStatus(HttpStatus.NOT_FOUND);
        resp.setContentType("text/plain");
        resp.getWriter().println("Route matched, but resource " + match.getPath() + " not found.");
        resp.getWriter().println("Matched route: " + match);
    }
}
