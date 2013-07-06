package restx.admin;

import com.google.common.collect.ImmutableSet;
import restx.HttpStatus;
import restx.RestxSession;
import restx.Status;
import restx.WebException;
import restx.annotations.DELETE;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.common.UUIDGenerator;
import restx.factory.Component;
import restx.security.PermitAll;
import restx.security.RestxPrincipal;

import javax.inject.Named;
import java.util.Map;

@Component @RestxResource(priority = 10000)
public class SessionResource {
    public static final RestxPrincipal RESTX_ADMIN_PRINCIPAL = new RestxPrincipal() {
        @Override
        public ImmutableSet<String> getPrincipalRoles() {
            return ImmutableSet.of(AdminPage.RESTX_ADMIN_ROLE);
        }

        @Override
        public String getName() {
            return "admin";
        }
    };

    private final String adminPasswordHash;

    public SessionResource(@Named("restx.admin.passwordHash") String adminPasswordHash) {
        this.adminPasswordHash = adminPasswordHash;
    }

    @PermitAll
    @POST("/sessions")
    public Session authenticate(Map session) {
        RestxSession.current().define(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, null);
        RestxSession.current().define(String.class, Session.SESSION_DEF_KEY, null);

        Map principal = (Map) session.get("principal");
        if (principal != null && "admin".equals(principal.get("name"))
                && adminPasswordHash.equals(principal.get("passwordHash"))) {
            String sessionKey = UUIDGenerator.generate();
            RestxSession.current().define(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, "admin");
            RestxSession.current().define(String.class, Session.SESSION_DEF_KEY, sessionKey);
            return new Session(sessionKey, RESTX_ADMIN_PRINCIPAL);
        } else {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }
    }

    @GET("/sessions/current")
    public Session currentSession() {
        String sessionKey = RestxSession.current().get(String.class, Session.SESSION_DEF_KEY).get();
        RestxPrincipal principal = RestxSession.current().get(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY).get();

        return new Session(sessionKey, principal);
    }

    @PermitAll
    @DELETE("/sessions/{sessionKey}")
    public Status logout(String sessionKey) {
        RestxSession.current().define(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY, null);
        RestxSession.current().define(String.class, Session.SESSION_DEF_KEY, null);
        return Status.of("logout");
    }
}
