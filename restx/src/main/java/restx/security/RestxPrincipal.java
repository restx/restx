package restx.security;

import com.google.common.collect.ImmutableSet;

import java.security.Principal;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 6:30 PM
 */
public interface RestxPrincipal extends Principal {
    public static final String SESSION_DEF_KEY = "principal";
    public ImmutableSet<String> getPrincipalRoles();
}
