package restx.annotations;

import restx.http.HttpStatus;

/**
 * @author fcamblor
 */
public @interface SuccessStatus {
    HttpStatus value() default HttpStatus.OK;
}
