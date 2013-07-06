package restx.security;

import com.google.common.base.Optional;
import restx.RestxRequest;

import java.util.Arrays;

/**
 * Provides a set of useful permissions, including the OPEN permission which is the only one that can allow access
 * to a resource without being authenticated.
 */
public class Permissions {
    private static final Permission OPEN = new Permission() {
        @Override
        public Optional<? extends Permission> has(RestxPrincipal principal, RestxRequest request) {
            return Optional.of(this);
        }

        @Override
        public String toString() {
            return "OPEN";
        }
    };
    private static final Permission IS_AUTHENTICATED = new Permission() {
        @Override
        public Optional<? extends Permission> has(RestxPrincipal principal, RestxRequest request) {
            return Optional.of(this);
        }

        @Override
        public String toString() {
            return "IS_AUTHENTICATED";
        }
    };

    /**
     * This is the only permission that can allow access to a resource without being authenticated.
     */
    public static Permission open() {
        return OPEN;
    }

    /**
     * This is the most basic permission which is true as soon as a principal is authenticated.
     */
    public static Permission isAuthenticated() {
        return IS_AUTHENTICATED;
    }


    /**
     * This permission is true as soon as the principal has the given role
     * @param role the role to check
     */
    public static Permission hasRole(final String role) {
        return new Permission() {
            public final String TO_STRING = "HAS_ROLE[" + role + "]";

            @Override
            public Optional<? extends Permission> has(RestxPrincipal principal, RestxRequest request) {
                return principal.getPrincipalRoles().contains(role) ? Optional.of(this) : Optional.<Permission>absent();
            }

            @Override
            public String toString() {
                return TO_STRING;
            }
        };
    }

    /**
     * A compound permission which is true if any of the underlying permissions is true
     */
    public static Permission anyOf(final Permission... permissions) {
        return new Permission() {
            @Override
            public Optional<? extends Permission> has(RestxPrincipal principal, RestxRequest request) {
                for (Permission permission : permissions) {
                    Optional<? extends Permission> p = permission.has(principal, request);
                    if (p.isPresent()) {
                        return p;
                    }
                }

                return Optional.absent();
            }

            @Override
            public String toString() {
                return "ANY_OF[" + Arrays.toString(permissions) + "]";
            }
        };
    }

    /**
     * A compound permission which is true if all underlying permissions are true
     */
    public static Permission allOf(final Permission... permissions) {
        return new Permission() {
            @Override
            public Optional<? extends Permission> has(RestxPrincipal principal, RestxRequest request) {
                for (Permission permission : permissions) {
                    Optional<? extends Permission> p = permission.has(principal, request);
                    if (!p.isPresent()) {
                        return Optional.absent();
                    }
                }

                return Optional.of(this);
            }

            @Override
            public String toString() {
                return "ALL_OF[" + Arrays.toString(permissions) + "]";
            }
        };
    }
}
