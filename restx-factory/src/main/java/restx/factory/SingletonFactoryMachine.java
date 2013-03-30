package restx.factory;

/**
 * A singleton factory machine provides a boundless component box made up of a component created
 * at instanciation time.
 *
 * This is better suited to tests, for production code prefer a SingleNameFactoryMachine which
 * constructs the component on demand (which is better especially if the component is overriden).
 */
public class SingletonFactoryMachine<C> extends SingleNameFactoryMachine<C> {
    public SingletonFactoryMachine(int priority, final NamedComponent<C> component) {
        super(priority, new NoDepsMachineEngine<C>(component.getName(), BoundlessComponentBox.FACTORY) {
            @Override
            public C doNewComponent(SatisfiedBOM satisfiedBOM) {
                return component.getComponent();
            }
        });
    }
}
