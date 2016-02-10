package restx.config;

import restx.admin.AdminModule;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.factory.Component;
import restx.security.RolesAllowed;

/**
 */
@RestxResource(group = "restx-admin") @Component
public class ConfigResource {
    private final RestxConfig config;

    public ConfigResource(RestxConfig config) {
        this.config = config;
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @GET("/@/config/elements")
    public Iterable<ConfigElement> findConfigElements() {
        return config.elements();
    }
}
