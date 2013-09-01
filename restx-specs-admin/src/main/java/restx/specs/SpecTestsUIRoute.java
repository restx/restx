package restx.specs;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.factory.Component;

@Component
public class SpecTestsUIRoute extends ResourcesRoute {
    public SpecTestsUIRoute() {
        super("SpecTestsUIRoute", "/@/ui/tests", "restx/specs/tests", ImmutableMap.of("", "index.html"));
    }
}