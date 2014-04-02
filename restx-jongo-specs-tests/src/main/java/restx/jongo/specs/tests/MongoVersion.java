/**
 *
 */
package restx.jongo.specs.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.flapdoodle.embed.mongo.distribution.Version;


/**
 * Specify embed mongo version
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoVersion {
    Version.Main value() default Version.Main.PRODUCTION;
}
