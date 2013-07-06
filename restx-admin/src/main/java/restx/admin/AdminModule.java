package restx.admin;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheLoader;
import com.google.common.hash.Hashing;
import restx.RestxSession;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.RestxPrincipal;

import javax.inject.Named;

@Module(priority = 10000)
public class AdminModule {
    @Provides
    @Named("restx.admin.password")
    public String restxAdminPassword() {
        return "juma";
    }

    @Provides
    @Named("restx.admin.passwordHash")
    public String restxAdminPasswordHash(@Named("restx.admin.password") String password) {
        return Hashing.md5().hashString(password, Charsets.UTF_8).toString();
    }

    @Provides
    @Named(RestxPrincipal.SESSION_DEF_KEY)
    public RestxSession.Definition.Entry principalSessionEntry() {
        return new RestxSession.Definition.Entry(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY,
                new CacheLoader<String, RestxPrincipal>() {
            @Override
            public RestxPrincipal load(String key) throws Exception {
                return "admin".equals(key) ? SessionResource.RESTX_ADMIN_PRINCIPAL : null;
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
