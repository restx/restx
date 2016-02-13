package restx.security;

import com.google.common.base.Optional;
import restx.factory.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a set of useful permissions, including the OPEN permission which is the only one that can allow access
 * to a resource without being authenticated.
 */
@Component
public class PermissionFactory {
    private static final Pattern ROLE_PARAM_INTERPOLATOR_REGEX = Pattern.compile("\\{(.+?)\\}");

    private static final Permission OPEN = new Permission() {
        @Override
        public Optional<? extends Permission> has(RestxPrincipal principal, Map<String, String> roleInterpolationMap) {
            return Optional.of(this);
        }

        @Override
        public String toString() {
            return "OPEN";
        }
    };
    private static final Permission IS_AUTHENTICATED = new Permission() {
        @Override
        public Optional<? extends Permission> has(RestxPrincipal principal, Map<String, String> roleInterpolationMap) {
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
    public Permission open() {
        return OPEN;
    }

    /**
     * This is the most basic permission which is true as soon as a principal is authenticated.
     */
    public Permission isAuthenticated() {
        return IS_AUTHENTICATED;
    }

    public boolean isOpen(Permission permission) {
        return permission == open();
    }

    public boolean isIsAuthenticated(Permission permission) {
        return permission == isAuthenticated();
    }

    /**
     * This permission is true as soon as the principal has the given role
     * @param role the role to check
     */
    public Permission hasRole(final String role) {
        return new Permission() {
            public final String TO_STRING = "HAS_ROLE[" + role + "]";

            @Override
            public Optional<? extends Permission> has(RestxPrincipal principal, Map<String, String> roleInterpolationMap) {
                if(principal.getPrincipalRoles().contains("*")) {
                    return Optional.of(this);
                }

                String interpolatedRole = interpolateRole(role, roleInterpolationMap);
                if(principal.getPrincipalRoles().contains(interpolatedRole)) {
                    return Optional.of(this);
                }

                return Optional.absent();
            }

            @Override
            public String toString() {
                return TO_STRING;
            }
        };
    }

    protected String interpolateRole(String role, Map<String, String> roleInterpolationMap) {
        Matcher matcher = ROLE_PARAM_INTERPOLATOR_REGEX.matcher(role);
        StringBuffer interpolatedRole = new StringBuffer();
        while(matcher.find()){
            String interpolationVarName = matcher.group(1);
            if(!roleInterpolationMap.containsKey(interpolationVarName)) {
                throw new IllegalArgumentException(String.format("Variable <%s> not found in role interpolation map <%s>",
                        interpolationVarName, roleInterpolationMap.toString()));
            }
            matcher.appendReplacement(interpolatedRole, roleInterpolationMap.get(interpolationVarName));
        }
        matcher.appendTail(interpolatedRole);
        return interpolatedRole.toString();
    }

    /**
     * A compound permission which is true if any of the underlying permissions is true
     */
    public Permission anyOf(final Permission... permissions) {
        return new Permission() {
            @Override
            public Optional<? extends Permission> has(RestxPrincipal principal, Map<String, String> roleInterpolationMap) {
                for (Permission permission : permissions) {
                    Optional<? extends Permission> p = permission.has(principal, roleInterpolationMap);
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
    public Permission allOf(final Permission... permissions) {
        return new Permission() {
            @Override
            public Optional<? extends Permission> has(RestxPrincipal principal, Map<String, String> roleInterpolationMap) {
                for (Permission permission : permissions) {
                    Optional<? extends Permission> p = permission.has(principal, roleInterpolationMap);
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
