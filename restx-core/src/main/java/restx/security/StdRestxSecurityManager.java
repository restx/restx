package restx.security;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxRequestMatch;
import restx.http.HttpStatus;
import restx.RestxRequest;
import restx.WebException;
import restx.factory.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple implementation of security manager which throws 401 WebException if
 * no principal is associated with current session and the permission is not , and 403 if no permission is matched.
 */
@Component
public class StdRestxSecurityManager implements RestxSecurityManager {
    private static final Logger logger = LoggerFactory.getLogger(StdRestxSecurityManager.class);

    protected final PermissionFactory permissionFactory;

    public StdRestxSecurityManager(PermissionFactory permissionFactory) {
        this.permissionFactory = permissionFactory;
    }

    @Override
    public void check(RestxRequest request, RestxRequestMatch requestMatch, Permission permission) {
        if (permissionFactory.isOpen(permission)) {
            return;
        }

        Optional<? extends RestxPrincipal> principal = RestxSession.current().getPrincipal();

        if (!principal.isPresent()) {
            logger.debug("no principal found: request={}", request);
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        Optional<? extends Permission> match = permission.has(principal.get(), createRoleInterpolationMapFrom(request, requestMatch));
        if (match.isPresent()) {
            logger.debug("permission matched: request={} principal={} perm={}", request, principal.get(), match.get());
            return;
        }

        logger.debug("permission not matched: request={} principal={} permission={}",
                request, principal.get(), permission);
        throw new WebException(HttpStatus.FORBIDDEN);
    }

    protected Map<String, String> createRoleInterpolationMapFrom(RestxRequest request, RestxRequestMatch match) {
        Map<String, String> roleInterpolationMap = new HashMap<>();

        if(request != null) {
            // When we have more than 1 query param value for a given key, subjectively keeping only the first one
            // I don't think these multi-values query params should be taken into consideration when interpolating roles
            // but I don't want to remove it from interpolation map either "if values.size()>1"
            roleInterpolationMap.putAll(Maps.transformValues(request.getQueryParams(), new Function<List<String>, String>(){
                @Override
                public String apply(List<String> input) {
                    return Iterables.getFirst(input, null);
                }
            }));
        }

        if(match != null) {
            roleInterpolationMap.putAll(match.getPathParams());
        }

        return roleInterpolationMap;
    }
}
