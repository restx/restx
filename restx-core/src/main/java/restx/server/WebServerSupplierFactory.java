package restx.server;

import restx.factory.*;

import java.util.Collections;
import java.util.Set;

/**
 * @author fcamblor
 */
@Machine
public class WebServerSupplierFactory implements FactoryMachine {

    private Factory.Query<WebServerSupplier> webServerSupplierQuery = Factory.Query.byClass(WebServerSupplier.class);

    @Override
    public boolean canBuild(Name<?> name) {
        return WebServerSupplier.class.isAssignableFrom(name.getClazz());
    }

    @Override
    public <T> MachineEngine<T> getEngine(final Name<T> name) {
        return new StdMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {
            @Override
            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                return (T) satisfiedBOM.getOne(webServerSupplierQuery).get().getComponent();
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(webServerSupplierQuery);
            }
        };
    }

    @Override
    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        return Collections.emptySet();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String toString() {
        return "WebServerSupplierFactory{" +
                "webServerSupplierQuery=" + webServerSupplierQuery +
                '}';
    }

}
