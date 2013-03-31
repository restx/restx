package restx.swagger;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.factory.Component;

import java.io.IOException;

@Component
public class SwaggerUIRoute implements RestxRoute {
    @Override
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        if ("GET".equals(req.getHttpMethod()) && "/@/api-docs-ui".equals(req.getRestxPath())) {
            resp.setContentType("text/html");

            ByteStreams.copy(
                    Resources.newInputStreamSupplier(Resources.getResource(SwaggerUIRoute.class, "swagger-ui.html")),
                    resp.getOutputStream());
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "GET /@/api-docs-ui => SwaggerUIRoute";
    }
}
