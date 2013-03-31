package restx.factory;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 1:43 AM
 */
public abstract class SingleComponentClassCustomizerEngine<E> extends SingleComponentCustomizerEngine<E> {
    private final Class<E> aClass;

    public SingleComponentClassCustomizerEngine(int priority, Class<E> aClass) {
        super(priority);
        this.aClass = aClass;
    }

    @Override
    public <T> boolean canCustomize(Name<T> name) {
        return name.getClazz() == aClass;
    }

    @Override
    public String toString() {
        return "SingleComponentClassCustomizerEngine{" +
                "class=" + aClass.getSimpleName() +
                '}';
    }
}
