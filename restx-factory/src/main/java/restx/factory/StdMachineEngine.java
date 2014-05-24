package restx.factory;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 2:47 PM
 */
public abstract class StdMachineEngine<T> implements MachineEngine<T> {
    private final Name<T> name;
    private final int priority;
    private final ComponentBox.BoxFactory boxFactory;

    protected StdMachineEngine(Name<T> name, ComponentBox.BoxFactory boxFactory) {
        this(name, 0, boxFactory);
    }

    protected StdMachineEngine(Name<T> name, int priority, ComponentBox.BoxFactory boxFactory) {
        this.name = name;
        this.priority = priority;
        this.boxFactory = boxFactory;
    }

    @Override
    public Name<T> getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    protected abstract T doNewComponent(SatisfiedBOM satisfiedBOM);

    @Override
    public ComponentBox<T> newComponent(SatisfiedBOM satisfiedBOM) {
        return boxFactory.of(new NamedComponent<>(name, priority, doNewComponent(satisfiedBOM)));
    }

    @Override
    public String toString() {
        return "StdMachineEngine{" +
                "name=" + name +
                ", priority=" + priority +
                ", boxFactory=" + boxFactory +
                ", bom=" + getBillOfMaterial() +
                '}';
    }
}
