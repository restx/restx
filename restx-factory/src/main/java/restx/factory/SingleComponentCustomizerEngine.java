package restx.factory;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 1:43 AM
 */
public abstract class SingleComponentCustomizerEngine<E> implements ComponentCustomizerEngine, ComponentCustomizer<E> {
    private final int priority;

    public SingleComponentCustomizerEngine(int priority) {
        this.priority = priority;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ComponentCustomizer<T> getCustomizer(Name<T> name) {
        return (ComponentCustomizer<T>) this;
    }

    @Override
    public int priority() {
        return priority;
    }
}
