package restx.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import restx.RestxRequest;
import restx.RestxRouteMatch;
import restx.StdEntityRoute;
import restx.StdRouteMatcher;
import restx.jackson.FrontObjectMapperFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/18/13
 * Time: 9:37 PM
 */
public class ErrorDescriptorsRoute extends StdEntityRoute {

    private final ImmutableMap<String, ErrorDescriptor> errorDescriptors;

    public ErrorDescriptorsRoute(Iterable<ErrorDescriptor> errorDescriptors,
                                 @Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper) {
        super("ErrorDescriptorsRoute", mapper, new StdRouteMatcher("GET", "/@/errors/descriptors"));
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
    protected Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException {
        return Optional.of(errorDescriptors.values());
    }
}
