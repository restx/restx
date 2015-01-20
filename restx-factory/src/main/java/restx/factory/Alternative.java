package restx.factory;

/**
 * Marks a class as an alternative implementation, to be used under certain conditions only.
 *
 * The {@link Name} used for the alternative is composed by:
 * - The class defined by {@link #to()}
 * - The name defined by {@link #named()} if defined, or the name of the component referenced by {@link #to()} if one is defined,
 * or the simple name of the class defined by {@link #to()}.
 */
public @interface Alternative {
    int priority() default -1000;
    Class<?> to();
    String named() default "";
}
