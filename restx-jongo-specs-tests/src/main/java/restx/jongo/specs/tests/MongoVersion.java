/**
 *
 */
package restx.jongo.specs.tests;

import de.flapdoodle.embed.mongo.distribution.Version;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Specify embed mongo version
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoVersion {
    Version.Main value() default Version.Main.PRODUCTION;
}
