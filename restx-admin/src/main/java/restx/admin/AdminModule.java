package restx.admin;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.*;

import javax.inject.Named;

@Module(priority = 10000)
public class AdminModule {
    public static final String RESTX_ADMIN_ROLE = "restx-admin";

    public static final RestxAdminPrincipal RESTX_ADMIN_PRINCIPAL = new RestxAdminPrincipal();

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
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(
            @Named("restx.admin.passwordHash") final String adminPasswordHash, SecuritySettings securitySettings) {
        return new StdBasicPrincipalAuthenticator(new UserService<RestxAdminPrincipal>() {
            @Override
            public Optional<RestxAdminPrincipal> findUserByName(String name) {
                return "admin".equals(name) ? Optional.of(RESTX_ADMIN_PRINCIPAL) : Optional.<RestxAdminPrincipal>absent();
            }

            @Override
            public Optional<RestxAdminPrincipal> findAndCheckCredentials(String name, String passwordHash) {
                return "admin".equals(name) && adminPasswordHash.equals(passwordHash) ?
                        Optional.of(RESTX_ADMIN_PRINCIPAL) : Optional.<RestxAdminPrincipal>absent();
            }
        }, securitySettings);
    }

    public static class RestxAdminPrincipal implements RestxPrincipal {
        @Override
        public ImmutableSet<String> getPrincipalRoles() {
            return ImmutableSet.of(RESTX_ADMIN_ROLE);
        }

        @Override
        public String getName() {
            return "admin";
        }
    }
}
