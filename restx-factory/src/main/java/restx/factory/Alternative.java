package restx.factory;

/**
 * Marks a class as an alternative implementation, to be used under certain conditions only.
 */
public @interface Alternative {
    int priority() default -1000;
    Class<?> to();
}
