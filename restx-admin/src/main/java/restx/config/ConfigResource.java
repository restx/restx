package restx.config;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.factory.Component;

/**
 */
@RestxResource @Component
public class ConfigResource {
    private final RestxConfig config;

    public ConfigResource(RestxConfig config) {
        this.config = config;
    }

    @GET("/@/config/elements")
    public Iterable<ConfigElement> findConfigElements() {
        return config.elements();
    }
}
