package restx.classloader;

import static java.lang.annotation.ElementType.TYPE;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation permits to declare a class as cold.
 *
 * Once annotated a class will not be hot-reloaded.
 *
 * @author apeyrard
 */
@Target(TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Cold {

}
