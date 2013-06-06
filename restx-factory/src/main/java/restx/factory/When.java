package restx.factory;

/**
 * A condition when an alternative should be used.
 *
 * This allows to active an alternative when a String component with name equals to the name attribute
 * has a value equal to the value attribute.
 */
public @interface When {
    String name();
    String value();
}
