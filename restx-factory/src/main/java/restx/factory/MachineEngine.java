package restx.factory;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 1:15 PM
 */
public interface MachineEngine<T> {
    Name<T> getName();
    int getPriority();
    BillOfMaterials getBillOfMaterial();
    ComponentBox<T> newComponent(SatisfiedBOM satisfiedBOM);
}
