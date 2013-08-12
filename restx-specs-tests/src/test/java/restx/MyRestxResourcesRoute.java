package restx;

import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

/**
 * @author fcamblor
 */
@Component
@RestxResource
@PermitAll
public class MyRestxResourcesRoute extends ResourcesRoute{

    public MyRestxResourcesRoute() {
        // Considering that every urls matching "/web/*" will be served looking
        // into the "static.*" package
        super("myResources", "web", "static");
    }
}

