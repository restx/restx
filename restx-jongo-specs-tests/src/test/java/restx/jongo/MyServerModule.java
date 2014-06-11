package restx.jongo;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.factory.Module;
import restx.factory.Provides;
import restx.mongo.MongoModule;
import restx.security.*;
import restx.server.JettyServerModule;

import javax.inject.Named;

/**
 * User: Christophe Labouisse
 * Date: 11/06/2014
 * Time: 17:27
 */
@Module
public class MyServerModule extends JettyServerModule {
    @Provides
    @Named("restx.server.jetty.webxml.default.location")
    public String restxServerJettyWebXmlDefaultLocation(@Named("restx.server.jetty.appbase.default.location") String appBase) {
        return appBase+"/WEB-INF/web.xml";
    }


    @Provides
    @Named("restx.server.jetty.appbase.default.location")
    public String restxServerJettyAppBaseDefaultLocation() {
        return "src/test/webapp";
    }

    @Provides
    @Named(MongoModule.MONGO_DB_NAME)
    public String dbName() {
        return "usercredentials-test";
    }

    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator() {
        return new BasicPrincipalAuthenticator() {
            @Override
            public Optional<? extends RestxPrincipal> findByName(String name) {
                return Optional.absent();
            }

            @Override
            public Optional<? extends RestxPrincipal> authenticate(String name, String passwordHash, ImmutableMap<String, ?> principalData) {
                return Optional.absent();
            }
        };
    }


}
