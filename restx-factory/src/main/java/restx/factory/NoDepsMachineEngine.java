package restx.factory;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 2:47 PM
 */
public abstract class NoDepsMachineEngine<T> extends StdMachineEngine<T> {

    protected NoDepsMachineEngine(Name<T> name, ComponentBox.BoxFactory boxFactory) {
        super(name, boxFactory);
    }
    protected NoDepsMachineEngine(Name<T> name, int priority, ComponentBox.BoxFactory boxFactory) {
        super(name, priority, boxFactory);
    }

    @Override
    public BillOfMaterials getBillOfMaterial() {
        return BillOfMaterials.EMPTY;
    }
}
