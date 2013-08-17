package restx.server;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module(priority = 1000)
public class JettyServerModule {
    @Provides
    @Named("restx.server.jetty.webxml.location")
    public String restxServerJettyWebXmlLocation(@Named("restx.server.jetty.appbase.location") String appBase) {
        return appBase+"/WEB-INF/web.xml";
    }


    @Provides
    @Named("restx.server.jetty.appbase.location")
    public String restxServerJettyAppBaseLocation() {
        return "src/main/webapp";
    }

    @Provides
    public WebServerSupplier webServerSupplier(
            @Named("restx.server.jetty.appbase.location") String appBase,
            @Named("restx.server.jetty.webxml.location") String webxml){
        return JettyWebServer.jettyWebServerSupplier(webxml, appBase);
    }
}
