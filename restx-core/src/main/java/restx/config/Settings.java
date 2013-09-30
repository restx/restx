package restx.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotate an interface with @Settings to mark it as a settings interface,
 * where all methods must be returning simple type and take no parameters,
 * and implementation is generated to look for values in RestxConfig.
 */
@Target(ElementType.TYPE)
public @interface Settings {
}
