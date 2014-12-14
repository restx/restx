package restx.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Defines a Settings key method in a Settings annotated interface.
 */
@Target(ElementType.METHOD)
public @interface SettingsKey {
    String key();
    String defaultValue() default "";
    String doc() default "";
}
