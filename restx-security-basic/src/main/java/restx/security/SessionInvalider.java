package restx.security;

import com.google.common.base.Optional;
import restx.factory.Component;

@Component
public class SessionInvalider {

    private RestxSession.Definition restxSessionDefinition;

    public SessionInvalider(RestxSession.Definition restxSessionDefinition) {
        this.restxSessionDefinition = restxSessionDefinition;
    }

    public void invalidateSession(){
        // Invalidating principal cache (on next login, principal roles will be refreshed)
        Optional<? extends RestxPrincipal> principal = RestxSession.current().getPrincipal();
        if (principal.isPresent()) {
            restxSessionDefinition.getEntry(RestxPrincipal.SESSION_DEF_KEY).get().invalidateCacheFor(principal.get().getName());
        }

        // Clearing principal
        RestxSession.current().clearPrincipal();
        RestxSession.current().define(String.class, Session.SESSION_DEF_KEY, null);
    }
}
