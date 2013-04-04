package restx.exceptions;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import restx.*;

import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/18/13
 * Time: 9:37 PM
 */
public class ErrorDescriptorsRoute extends StdRoute {

    private final ImmutableMap<String, ErrorDescriptor> errorDescriptors;

    public ErrorDescriptorsRoute(Iterable<ErrorDescriptor> errorDescriptors) {
        super("ErrorDescriptorsRoute", new StdRouteMatcher("GET", "/@/errors/descriptors"));
        Map<String, ErrorDescriptor> map = Maps.newLinkedHashMap();
        for (ErrorDescriptor errorDescriptor : errorDescriptors) {
            if (map.containsKey(errorDescriptor.getErrorCode())) {
                throw new IllegalStateException("duplicate error code found: " + errorDescriptor.getErrorCode());
            }
            map.put(errorDescriptor.getErrorCode(), errorDescriptor);
        }
        this.errorDescriptors = ImmutableMap.copyOf(map);
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println(Joiner.on("\n").join(errorDescriptors.values()));
    }
}
