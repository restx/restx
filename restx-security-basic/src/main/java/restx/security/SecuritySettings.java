package restx.security;

import restx.config.Settings;
import restx.config.SettingsKey;

/**
 * Date: 14/12/13
 * Time: 14:50
 */
@Settings
public interface SecuritySettings {
    @SettingsKey(key = "restx.security.rememberMe.duration", defaultValue = "30",
            doc = "the duration in days during which authentication should be remembered " +
                    "when using rememberme feature with StdBasicPrincipalAuthenticator")
    int rememberMeDuration();
}
