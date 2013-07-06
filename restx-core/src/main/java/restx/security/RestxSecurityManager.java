package restx.security;

import restx.RestxRequest;

/**
 * A security manager is responsible for checking if the principal associated with a given request (or current session)
 * has the permission to access a resource.
 */
public interface RestxSecurityManager {
    /**
     * Checks if the principal associated with the given request has the given permission.
     *
     * The security manager can safely assume that a RestxSession is available in current thread context.
     *
     * @param request the request for which the check is performed
     * @param permission the permission to check. must not be null.
     */
    void check(RestxRequest request, Permission permission);
}
