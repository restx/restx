package restx.server;

import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module(priority = 1000)
public class JettyServerModule {
    @Provides
    @Named("restx.server.jetty.webxml.default.location")
    public String restxServerJettyWebXmlDefaultLocation(@Named("restx.server.jetty.appbase.default.location") String appBase) {
        return appBase + "/WEB-INF/web.xml";
    }


    @Provides
    @Named("restx.server.jetty.appbase.default.location")
    public String restxServerJettyAppBaseDefaultLocation() {
        return "src/main/webapp";
    }

    @Provides
    @Named("restx.server.jetty")
    public WebServerSupplier jettyWebServerSupplier(
            @Named("restx.server.jetty.appbase.default.location") String appBase,
            @Named("restx.server.jetty.webxml.default.location") String webxml) {
        return JettyWebServer.jettyWebServerSupplier(webxml, appBase);
    }
}
