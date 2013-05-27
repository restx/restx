package restx.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tells where a set of specs should be found.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FindSpecsIn {
    String value();
}
