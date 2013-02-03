package restx.factory;

/**
 * A singleton factory machine provides a boundless component box made up of a component created
 * at instanciation time.
 *
 * This is better suited to tests, for production code prefer a SingleNameFactoryMachine which
 * constructs the component on demand (which is better especially if the component is overriden).
 */
public class SingletonFactoryMachine<C> extends SingleNameFactoryMachine<C> {
    private final NamedComponent<C> component;

    public SingletonFactoryMachine(int priority, NamedComponent<C> component) {
        super(priority, component.getName(), BoundlessComponentBox.FACTORY);
        this.component = component;
    }

    @Override
    protected C doNewComponent(Factory factory) {
        return component.getComponent();
    }

    @Override
    public String toString() {
        return "SingletonFactoryMachine{" +
                "priority=" + priority() +
                ", name=" + name +
                ", component=" + component +
                '}';
    }
}
