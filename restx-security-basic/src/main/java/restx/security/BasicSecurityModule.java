package restx.security;

import com.google.common.base.Function;
import com.google.common.base.Optional;
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
        return new DefaultSessionDefinitionEntry<>(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY,
                new Function<String, Optional<? extends RestxPrincipal>>() {
                    @Override
                    public Optional<? extends RestxPrincipal> apply(String key) {
                        return authenticator.findByName(key);
                    }
        });
    }

    @Provides
    @Named(Session.SESSION_DEF_KEY)
    public RestxSession.Definition.Entry sessionKeySessionEntry() {
        return new DefaultSessionDefinitionEntry<>(String.class, Session.SESSION_DEF_KEY,
                new Function<String, Optional<? extends String>>() {
                    @Override
                    public Optional<? extends String> apply(String key) {
                        return Optional.fromNullable(key);
                    }
                });
    }
}
