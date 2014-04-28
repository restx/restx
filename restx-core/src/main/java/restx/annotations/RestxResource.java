package restx.annotations;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 10:48 PM
 */
public @interface RestxResource {

    /**
     * Root path for all routes of the resource.
     *
     * For example, if a class is annotated with {@code @RestxResource("/test")}
     * and contains a method annotated with {@code @GET("/message")}
     * the resource path of the method is {@code /test/message}.
     *
     * @return the root path of the resource
     */
    String value() default "";
    String group() default "default";
    int priority() default 0;
}
