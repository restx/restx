package restx.exceptions;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.factory.Component;

/**
 * User: xavierhanin
 * Date: 4/7/13
 * Time: 4:03 PM
 */
@Component
public class ErrorsUIRoute extends ResourcesRoute {
    public ErrorsUIRoute() {
        super("ErrorsUIRoute", "/@/ui/errors", "restx/exceptions", ImmutableMap.of("", "index.html"));
    }
}
