package restx.annotations;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 10:48 PM
 */
public @interface RestxResource {
    String group() default "default";
}
