package restx.factory;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;

/**
 * User: xavierhanin
 * Date: 4/7/13
 * Time: 4:03 PM
 */
@Component
public class FactoryUIRoute extends ResourcesRoute {
    public FactoryUIRoute() {
        super("FactoryUIRoute", "/@/ui/factory", "restx/factory", ImmutableMap.of("", "index.html"));
    }
}
