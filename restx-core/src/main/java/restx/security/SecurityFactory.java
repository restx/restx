package restx.security;

import com.google.common.base.Optional;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * @author fcamblor
 */
@Module(priority = 1000)
public class SecurityFactory {
    @Provides
    public RestxSessionCookieDescriptor restxSessionCookieDescriptor(@Named("app.name") Optional<String> appName){
        if(appName.isPresent()){
            return new RestxSessionCookieDescriptor(
                    String.format("%s-%s", "RestxSession", appName),
                    String.format("%s-%s", "RestxSessionSignature", appName));
        } else {
            // Keeping backward compatibility when appName is not provided
            return new RestxSessionCookieDescriptor("RestxSession", "RestxSessionSignature");
        }
    }
}
