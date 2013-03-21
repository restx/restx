package restx.exceptions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 2:32 PM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorCode {
    /**
     * error code.
     * @return
     */
    String code();

    /**
     * error description.
     * @return
     */
    String description();

    /**
     * HTTP error status.
     * @return
     */
    int status() default 400;
}
