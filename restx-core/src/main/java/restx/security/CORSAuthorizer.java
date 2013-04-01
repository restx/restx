package restx.security;

import com.google.common.base.Optional;
import restx.RestxRequest;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 1:14 PM
 */
public interface CORSAuthorizer {
    /**
     * Authorizes a CORS request or not.
     *
     * Several authorizers can be checked one after the other, an authorizer has to return Optional.absent() to
     * indicate it doesn't take the decision for the request.
     *
     *
     * @param request the request which triggered the check (either an OPTIONS, a GET or a POST)
     * @param origin the origin of the request
     * @param method the HTTP method to check
     * @param restxPath the restx path to check
     * @return a CORS object if the request must be authorized or not, or Optional.absent() if this authorizer doesn't
     * take the decision.
     */
    Optional<CORS> checkCORS(RestxRequest request, String origin, String method, String restxPath);
}
