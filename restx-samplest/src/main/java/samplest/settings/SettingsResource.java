package samplest.settings;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

/**
 * Date: 8/12/13
 * Time: 22:10
 */
@RestxResource @Component
public class SettingsResource {
    private final MySettings settings;

    public SettingsResource(MySettings settings) {
        this.settings = settings;
    }

    @PermitAll @GET("/settings/key1")
    public String getSettingsKey1() {
        return settings.key1().or("NONE");
    }
    @PermitAll @GET("/settings/key2")
    public String getSettingsKey2() {
        return settings.key2();
    }
    @PermitAll @GET("/settings/key3")
    public String getSettingsKey3() {
        return settings.key3();
    }
}
