package restx.exceptions;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.factory.*;

import java.io.IOException;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/18/13
 * Time: 9:37 PM
 */
public class ErrorDescriptorsRoute implements RestxRoute {

    private final ImmutableMap<String, ErrorDescriptor> errorDescriptors;

    public ErrorDescriptorsRoute(Iterable<ErrorDescriptor> errorDescriptors) {
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
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        if ("GET".equals(req.getHttpMethod()) && "/@/errors/descriptors".equals(req.getRestxPath())) {
            resp.setContentType("text/plain");
            resp.getWriter().println(Joiner.on("\n").join(errorDescriptors.values()));
            return true;
        }

        return false;
    }

    public static class Factory extends SingleNameFactoryMachine<ErrorDescriptorsRoute> {
        private static final restx.factory.Factory.Query<ErrorDescriptor> ERROR_DESCRIPTOR_QUERY =
                                                restx.factory.Factory.Query.byClass(ErrorDescriptor.class);

        public Factory() {
            super(0, new StdMachineEngine<ErrorDescriptorsRoute>(
                    Name.of(ErrorDescriptorsRoute.class, "SpecRecorderRoute"), BoundlessComponentBox.FACTORY) {
                @Override
                protected ErrorDescriptorsRoute doNewComponent(SatisfiedBOM satisfiedBOM) {
                    return new ErrorDescriptorsRoute(satisfiedBOM.getAsComponents(ERROR_DESCRIPTOR_QUERY));
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return BillOfMaterials.of(ERROR_DESCRIPTOR_QUERY);
                }
            });
        }
    }
}
