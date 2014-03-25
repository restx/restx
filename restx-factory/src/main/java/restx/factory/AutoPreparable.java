package restx.factory;

/**
 * An AutoPreparable component is prepared when the factory is prepared.
 *
 * This is very similar to AutoStartable, except that usually AutoStartable components are more heavyweight,
 * and AutoPreparable are more lightweight.
 *
 * AutoStartable components are started at server startup in all modes but the test mode.
 * AutoPreparable components are prepared at server startup in prod mode, but in other modes like dev mode
 * they are prepared per request, and thus do not break hot reloading of the components and their dependencies.
 *
 * For more details on what this means and components lifecycles see
 * http://restx.io/docs/ref-lifecycle.html
 */
public interface AutoPreparable {
    public void prepare();
}
