package restx.factory;

/**
 * An AutoStartable component is started when the factory is started.
 *
 * For more details on what this means and components lifecycles see
 * http://restx.io/docs/ref-lifecycle.html
 */
public interface AutoStartable {
    public void start();
}
