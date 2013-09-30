package restx.config;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.factory.Component;

/**
 */
@Component
public class ConfigUIRoute extends ResourcesRoute {
    public ConfigUIRoute() {
        super("ConfigUIRoute", "/@/ui/config", "restx/config", ImmutableMap.of("", "index.html"));
    }
}
