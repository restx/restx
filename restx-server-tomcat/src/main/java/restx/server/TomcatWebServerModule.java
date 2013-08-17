package restx.server;

import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module(priority = 1000)
public class TomcatWebServerModule {

    @Provides
    @Named("restx.server.tomcat.appbase.location")
    public String restxServerTomcatAppBaseLocation() {
        return "src/main/webapp";
    }

    @Provides
    @Named("restx.server.tomcat")
    public WebServerSupplier webServerSupplier(@Named("restx.server.tomcat.appbase.location") String appBase){
        return TomcatWebServer.tomcatWebServerSupplier(appBase);
    }
}
