package restx.swagger;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import restx.*;
import restx.factory.Component;

import java.io.IOException;

@Component
public class SwaggerUIRoute extends StdRoute {
    public SwaggerUIRoute() {
        super("SwaggerUIRoute", new StdRouteMatcher("GET", "/@/api-docs-ui"));
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setContentType("text/html");

        ByteStreams.copy(
                Resources.newInputStreamSupplier(Resources.getResource(SwaggerUIRoute.class, "swagger-ui.html")),
                resp.getOutputStream());
    }
}
