package restx.security;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.http.HttpStatus;
import restx.RestxRequest;
import restx.WebException;
import restx.factory.Component;

/**
 * A simple implementation of security manager which throws 401 WebException if
 * no principal is associated with current session and the permission is not , and 403 if no permission is matched.
 */
@Component
public class StdRestxSecurityManager implements RestxSecurityManager {
    private final Logger logger = LoggerFactory.getLogger(StdRestxSecurityManager.class);

    @Override
    public void check(RestxRequest request, Permission permission) {
        if (permission == Permissions.open()) {
            return;
        }

        Optional<? extends RestxPrincipal> principal = RestxSession.current().getPrincipal();

        if (!principal.isPresent()) {
            logger.debug("no principal found: request={}", request);
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        Optional<? extends Permission> match = permission.has(principal.get(), request);
        if (match.isPresent()) {
            logger.debug("permission matched: request={} principal={} perm={}", request, principal.get(), match.get());
            return;
        }

        logger.debug("permission not matched: request={} principal={} permission={}",
                request, principal.get(), permission);
        throw new WebException(HttpStatus.FORBIDDEN);
    }
}
