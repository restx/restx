package restx.servlet;

import restx.security.RestxPrincipal;

import java.security.Principal;

/**
 * Date: 23/4/14
 * Time: 20:56
 */
public interface ServletPrincipalConverter {
    /**
     * Converts a standard principal to a RestxPrincipal.
     *
     * The given principal must not already be a RestxPrincipal.
     *
     * @param principal the principal to convert. Must not be null.
     * @return the converted principal. Never null.
     */
    RestxPrincipal toRestxPrincipal(Principal principal);
}
