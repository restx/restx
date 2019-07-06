package restx.security;

import com.google.common.base.Optional;
import restx.common.RestxConfig;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module(priority = 1000)
public class SecurityFactory {
    @Provides
    public RestxSessionCookieDescriptor restxSessionCookieDescriptor(@Named("app.name") Optional<String> appName,
                                                                     RestxConfig restxConfig) {
        if(appName.isPresent()){
            return new RestxSessionCookieDescriptor(
                    String.format("%s-%s", "RestxSession", appName.get()),
                    String.format("%s-%s", "RestxSessionSignature", appName.get()),
                    !restxConfig.getBoolean("cookie.disable.encode").or(Boolean.FALSE));
        } else {
            // Keeping backward compatibility when appName is not provided
            return new RestxSessionCookieDescriptor(
                    "RestxSession",
                    "RestxSessionSignature",
                    !restxConfig.getBoolean("cookie.disable.encode").or(Boolean.FALSE));
        }
    }
}
