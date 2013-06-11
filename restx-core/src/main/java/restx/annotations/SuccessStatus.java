package restx.annotations;

import restx.HttpStatus;

/**
 * @author fcamblor
 */
public @interface SuccessStatus {
    HttpStatus value() default HttpStatus.OK;
}
