package restx.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to identitify that a resource class or method can be accessed without principal.
 *
 * By default restx endpoints are secured, this annotation is used to allow unsecured access.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PermitAll {
}
