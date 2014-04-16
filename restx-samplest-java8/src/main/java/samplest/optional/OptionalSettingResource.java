package samplest.optional;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import samplest.settings.MySettings;

import java.util.Optional;

@RestxResource
@Component
public class OptionalSettingResource {

    final MySettings settings;

    public OptionalSettingResource(MySettings settings) {
        this.settings = settings;
    }

    @GET("/optional/settings/key1")
    public Optional<String> key1() {
        return settings.key1();
    }
    @GET("/optional/settings/key2")
    public Optional<String> key2() {
        return settings.key2();
    }
}
