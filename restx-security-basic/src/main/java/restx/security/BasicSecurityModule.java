package restx.security;

import com.google.common.cache.CacheLoader;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 */
@Module(priority = 10000)
public class BasicSecurityModule {
    @Provides
    @Named(RestxPrincipal.SESSION_DEF_KEY)
    public RestxSession.Definition.Entry principalSessionEntry(final BasicPrincipalAuthenticator authenticator) {
        return new RestxSession.Definition.Entry(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY,
                new CacheLoader<String, RestxPrincipal>() {
            @Override
            public RestxPrincipal load(String key) throws Exception {
                return authenticator.findByName(key).orNull();
            }
        });
    }

    @Provides
    public RestxSession.Definition.Entry sessionKeySessionEntry() {
        return new RestxSession.Definition.Entry(String.class, Session.SESSION_DEF_KEY,
                new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return key;
            }
        });
    }
}
