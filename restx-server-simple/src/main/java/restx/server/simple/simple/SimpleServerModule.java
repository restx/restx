package restx.server.simple.simple;

import restx.factory.Module;
import restx.factory.Provides;
import restx.server.WebServerSupplier;

/**
 * @author fcamblor
 */
@Module(priority = 1000)
public class SimpleServerModule {
    @Provides
    public WebServerSupplier webServerSupplier(){
        return SimpleWebServer.simpleWebServerSupplier();
    }
}
