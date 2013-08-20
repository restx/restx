package restx.server.simple.simple;

import restx.factory.Module;
import restx.factory.Provides;
import restx.server.WebServerSupplier;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module(priority = 1000)
public class SimpleServerModule {
    @Provides
    @Named("restx.server.simple")
    public WebServerSupplier simpleWebServerSupplier(){
        return SimpleWebServer.simpleWebServerSupplier();
    }
}
