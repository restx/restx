package restx;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import restx.common.RestxConfig;
import restx.factory.Component;

import java.util.Collection;

/**
 */
@Component(priority = 1000)
public class HttpSettingsConfig implements HttpSettings {
    private final RestxConfig config;

    public HttpSettingsConfig(RestxConfig config) {
        this.config = config;
    }

    @Override
    public Collection<String> forwardedSupport() {
        return Splitter.on(",").trimResults().splitToList(
                config.getString("restx.http.XForwardedSupport").or("127.0.0.1"));
    }

    @Override
    public Optional<String> host() {
        return config.getString("restx.http.host");
    }

    @Override
    public Optional<String> scheme() {
        return config.getString("restx.http.scheme");
    }
}
