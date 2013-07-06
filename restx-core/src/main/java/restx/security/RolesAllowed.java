package restx.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to identify the roles allowed to access a resource endpoint.
 */
@Target(ElementType.METHOD)
public @interface RolesAllowed {
    String[] value();
}
