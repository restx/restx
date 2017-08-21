package restx.security;

import com.google.common.base.Optional;
import restx.factory.Component;

@Component
public class CurrentSessionResolver {
    public Optional<Session> resolveCurrentSession(){
        Optional<String> sessionKey = RestxSession.current().get(String.class, Session.SESSION_DEF_KEY);
        Optional<? extends RestxPrincipal> principal = RestxSession.current().getPrincipal();

        if(!sessionKey.isPresent() || !principal.isPresent()) {
            return Optional.absent();
        }

        return Optional.of(new Session(sessionKey.get(), principal.get()));
    }
}
