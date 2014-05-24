package restx.factory;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:53 PM
 */
public class MachineEngineWrapper<T> implements MachineEngine<T> {
    private final MachineEngine<T> original;

    public MachineEngineWrapper(MachineEngine<T> original) {
        this.original = original;
    }

    public Name<T> getName() {
        return original.getName();
    }

    @Override
    public int getPriority() {
        return original.getPriority();
    }

    public BillOfMaterials getBillOfMaterial() {
        return original.getBillOfMaterial();
    }

    public ComponentBox<T> newComponent(SatisfiedBOM satisfiedBOM) {
        return original.newComponent(satisfiedBOM);
    }

    @Override
    public String toString() {
        return "MachineEngineWrapper{" +
                "original=" + original +
                '}';
    }
}
