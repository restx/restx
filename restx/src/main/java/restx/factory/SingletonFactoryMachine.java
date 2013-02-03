package restx.factory;

/**
 * User: xavierhanin
 * Date: 2/1/13
 * Time: 3:18 PM
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
