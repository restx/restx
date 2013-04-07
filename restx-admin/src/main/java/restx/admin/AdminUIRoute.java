package restx.admin;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.factory.Component;

@Component(priority = 1000)
public class AdminUIRoute extends ResourcesRoute {
    public AdminUIRoute() {
        super("AdminUIRoute", "/@/ui", "restx/admin", ImmutableMap.of("", "index.html"));
    }
}
