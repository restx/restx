package restx.factory;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:38 PM
 */
public @interface Component {
    int priority() default 0;
}
