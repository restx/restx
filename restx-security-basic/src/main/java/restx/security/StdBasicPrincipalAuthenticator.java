package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.joda.time.Duration;

/**
 * Date: 14/12/13
 * Time: 14:46
 */
public class StdBasicPrincipalAuthenticator implements BasicPrincipalAuthenticator {
    private final UserService<?> users;
    private final SecuritySettings securitySettings;

    public StdBasicPrincipalAuthenticator(UserService<?> users, SecuritySettings securitySettings) {
        this.users = users;
        this.securitySettings = securitySettings;
    }

    @Override
    public Optional<? extends RestxPrincipal> findByName(String name) {
        return users.findUserByName(name);
    }

    @Override
    public Optional<? extends RestxPrincipal> authenticate(String name, String passwordHash,
                                                           ImmutableMap<String, ?> principalData) {
        boolean rememberMe = Boolean.valueOf((String) principalData.get("rememberMe"));

        Optional<? extends RestxPrincipal> u = users.findAndCheckCredentials(name, passwordHash);
        if (u.isPresent()) {
            RestxSession.current().expires(rememberMe
                    ? Duration.standardDays(securitySettings.rememberMeDuration()) : Duration.ZERO);
        }

        return u;
    }
}
