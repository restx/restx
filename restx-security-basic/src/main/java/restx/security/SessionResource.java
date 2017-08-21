package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import restx.common.UUIDGenerator;
import restx.http.HttpStatus;
import restx.Status;
import restx.WebException;
import restx.annotations.DELETE;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;

import java.util.Map;

@Component @RestxResource(priority = 10000)
public class SessionResource {

    private final BasicPrincipalAuthenticator authenticator;
    private final UUIDGenerator uuidGenerator;
    private SessionInvalider sessionInvalider;

    public SessionResource(BasicPrincipalAuthenticator authenticator, UUIDGenerator uuidGenerator, SessionInvalider sessionInvalider) {
        this.authenticator = authenticator;
        this.uuidGenerator = uuidGenerator;
        this.sessionInvalider = sessionInvalider;
    }


    @PermitAll
    @POST("/sessions")
    public Session authenticate(Map session) {
        RestxSession.current().clearPrincipal();
        RestxSession.current().define(String.class, Session.SESSION_DEF_KEY, null);

        Map<String, ?> principal = getPrincipal(session);
        if (principal == null) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        String name = (String) principal.get("name");
        String passwordHash = (String) principal.get("passwordHash");

        Optional<? extends RestxPrincipal> principalOptional = authenticator.authenticate(
                name, passwordHash, ImmutableMap.copyOf(principal));

        if (principalOptional.isPresent()) {
            String sessionKey = uuidGenerator.doGenerate();
            RestxSession.current().authenticateAs(principalOptional.get());
            RestxSession.current().define(String.class, Session.SESSION_DEF_KEY, sessionKey);
            return new Session(sessionKey, principalOptional.get());
        } else {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, ?> getPrincipal(Map session) {
        return (Map<String, ?>) session.get("principal");
    }

    @GET("/sessions/current")
    public Optional<Session> currentSession() {
        Optional<String> sessionKey = RestxSession.current().get(String.class, Session.SESSION_DEF_KEY);
        Optional<? extends RestxPrincipal> principal = RestxSession.current().getPrincipal();

        if(!sessionKey.isPresent() || !principal.isPresent()) {
            return Optional.absent();
        }

        return Optional.of(new Session(sessionKey.get(), principal.get()));
    }

    @PermitAll
    @DELETE("/sessions/{sessionKey}")
    public Status logout(String sessionKey) {
        sessionInvalider.invalidateSession();
        return Status.of("logout");
    }
}
