package samplest;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import restx.admin.AdminModule;
import restx.factory.Module;
import restx.factory.Provides;
import restx.i18n.SupportedLocale;
import restx.security.*;

import javax.inject.Named;
import java.util.Locale;

/**
 * Date: 1/12/13
 * Time: 14:45
 */
@Module
public class AppModule {
    private ImmutableMap<String, RestxPrincipal> principals = ImmutableMap.of(
            "admin", AdminModule.RESTX_ADMIN_PRINCIPAL,
            "user1", new StdUser("user1", ImmutableSet.<String>of("hello"))
    );

    @Provides @Named("restx.app.package")
    public String appPackage() {
        return "samplest";
    }

    @Provides
    public SupportedLocale french() {
        return new SupportedLocale(Locale.FRENCH);
    }

    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(SecuritySettings securitySettings,
                                                                   final @Named("restx.admin.passwordHash") String adminPasswordHash) {
        return new StdBasicPrincipalAuthenticator(new UserService<RestxPrincipal>() {
            @Override
            public Optional<RestxPrincipal> findUserByName(String name) {
                return Optional.fromNullable(principals.get(name));
            }

            @Override
            public Optional<RestxPrincipal> findAndCheckCredentials(String name, String passwordHash) {
                RestxPrincipal principal = principals.get(name);
                if (principal == null || !adminPasswordHash.equals(passwordHash)) {
                    return Optional.absent();
                } else {
                    return Optional.of(principal);
                }
            }
        }, securitySettings);
    }

}
