package restx.factory;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 1:43 AM
 */
public abstract class SingleComponentNameCustomizerEngine<E> extends SingleComponentCustomizerEngine<E> {
    private final Name<E> name;

    public SingleComponentNameCustomizerEngine(int priority, Name<E> name) {
        super(priority);
        this.name = name;
    }

    @Override
    public <T> boolean canCustomize(Name<T> name) {
        return name.equals(this.name);
    }

    @Override
    public String toString() {
        return "SingleComponentNameCustomizerEngine{" +
                "name=" + name +
                '}';
    }
}
