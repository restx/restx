package restx.jongo;

import org.jongo.Jongo;
import restx.factory.*;

import java.util.Collections;
import java.util.Set;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 9:04 AM
*/
@Machine
public class JongoCollectionFactory implements FactoryMachine {

    private Factory.Query<Jongo> jongoQuery = Factory.Query.byName(JongoModule.NAME);

    @Override
    public boolean canBuild(Name<?> name) {
        return JongoCollection.class == name.getClazz();
    }

    @Override
    public <T> MachineEngine<T> getEngine(final Name<T> name) {
        return new StdMachineEngine<T>(name, priority(), BoundlessComponentBox.FACTORY) {
            @Override
            @SuppressWarnings("unchecked")
            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                return (T) new StdJongoCollection(satisfiedBOM.getOne(jongoQuery).get().getComponent(),
                                name.getName());
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(jongoQuery);
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
        return "JongoCollectionFactory{" +
                "jongoQuery=" + jongoQuery +
                '}';
    }
}
