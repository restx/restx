package restx.security;

import com.google.common.collect.ImmutableSet;

import java.security.Principal;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 6:30 PM
 */
public interface RestxPrincipal extends Principal {
    public static final String CTX_KEY = "principal";
    public ImmutableSet<String> getPrincipalRoles();
}
