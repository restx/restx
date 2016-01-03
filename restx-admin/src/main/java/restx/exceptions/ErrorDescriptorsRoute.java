package restx.exceptions;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.StdRestxRequestMatcher;
import restx.factory.Component;
import restx.jackson.FrontObjectMapperFactory;
import restx.jackson.StdJsonProducerEntityRoute;

import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/18/13
 * Time: 9:37 PM
 */
@Component
public class ErrorDescriptorsRoute extends StdJsonProducerEntityRoute {

    private final ImmutableMap<String, ErrorDescriptor> errorDescriptors;

    public ErrorDescriptorsRoute(Iterable<ErrorDescriptor> errorDescriptors,
                                 @Named(FrontObjectMapperFactory.WRITER_NAME) ObjectWriter objectWriter) {
        super("ErrorDescriptorsRoute", ImmutableCollection.class, objectWriter, new StdRestxRequestMatcher("GET", "/@/errors/descriptors"));
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
    protected Optional<?> doRoute(RestxRequest restxRequest, RestxResponse restxResponse, RestxRequestMatch match, Object i) throws IOException {
        return Optional.of(errorDescriptors.values());
    }
}
