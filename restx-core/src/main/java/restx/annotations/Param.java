package restx.annotations;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 3:55 PM
 */
public @interface Param {
    public static enum Kind {
        PATH, QUERY, BODY, CONTEXT, DEFAULT
    }

    String value() default "";
    Kind kind() default Kind.DEFAULT;
}
