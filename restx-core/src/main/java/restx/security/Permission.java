package restx.security;

import com.google.common.base.Optional;

import java.util.Map;

/**
 * A permission is a generic security concept, used to check if a principal is allowed to access a resource.
 */
public interface Permission {
    /**
     * Return absent if the permission is not met, or the matching permission if met.
     *
     * The returned permission is not always the same as this permission, in case of compound permissions it may be
     * more specific.
     *
     * @param principal the principal to check
     * @param roleInterpolationMap a role interpolation map which may be used to interpolate 'dynamic' roles
     * @return absent if not matched, the matching permission otherwise.
     */
    Optional<? extends Permission> has(RestxPrincipal principal, Map<String, String> roleInterpolationMap);
}
