package restx.apidocs;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.factory.Component;

@Component
public class ApiDocsUIRoute extends ResourcesRoute {
    public ApiDocsUIRoute() {
        super("ApiDocsUIRoute", "/@/api-docs-ui", "restx/apidocs", ImmutableMap.of("", "index.html"));
    }
}
