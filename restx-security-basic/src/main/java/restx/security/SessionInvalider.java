package restx.security;

import restx.factory.Component;

@Component
public class SessionInvalider {

    public SessionInvalider() {
    }

    public void invalidateSession(){
        // Clearing principal
        RestxSession.current().clearPrincipal();
        RestxSession.current().define(String.class, Session.SESSION_DEF_KEY, null);
    }
}
