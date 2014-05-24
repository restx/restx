package restx.factory;

/**
 * User: xavierhanin
 * Date: 2/1/13
 * Time: 1:41 PM
 */
public @interface Provides {
    /**
     * The priority to set on the provided component.
     *
     * Note that 0 has a very special meaning here, meaning that the provided component should inherit
     * priority from the @Module definition.
     *
     * So if you want to have different priorities for different components in the same module, and some of them
     * be the 0 priority, you MUST keep the @Module priority set to 0, and set the exceptions in the @Provides.
     *
     * @return the priority of the provided component.
     */
    int priority() default 0;
}
