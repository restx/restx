package restx.i18n.admin;

import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.factory.Component;

/**
 */
@Component
public class MessagesUIRoute extends ResourcesRoute {
    public MessagesUIRoute() {
        super("MessagesUIRoute", "/@/ui/messages", "restx/i18n", ImmutableMap.of("", "index.html"));
    }
}
