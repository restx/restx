/**
 *
 */
package restx.jongo.specs.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specify embed mongo version
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoVersion {
    String DEFAULT_MONGO_VERSION = "6.0";

    String value() default DEFAULT_MONGO_VERSION;
}

