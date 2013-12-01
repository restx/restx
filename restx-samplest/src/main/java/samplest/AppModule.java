package samplest;

import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * Date: 1/12/13
 * Time: 14:45
 */
@Module
public class AppModule {
    @Provides @Named("restx.app.package")
    public String appPackage() {
        return "samplest";
    }
}
