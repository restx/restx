package restx.jongo;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import restx.factory.*;

import java.util.Collections;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:21 PM
 */
public class StdJongoCollection implements JongoCollection {
    private final Jongo jongo;
    private final String name;

    public StdJongoCollection(Jongo jongo, String name) {
        this.jongo = jongo;
        this.name = name;
    }

    @Override
    public MongoCollection get() {
        return jongo.getCollection(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public static class JongoCollectionFactory implements FactoryMachine {

        private Factory.Query<Jongo> jongoQuery = Factory.Query.byName(JongoFactory.NAME);

        @Override
        public boolean canBuild(Name<?> name) {
            return JongoCollection.class.isAssignableFrom(name.getClazz());
        }

        @Override
        public <T> MachineEngine<T> getEngine(final Name<T> name) {
            return new StdMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {
                @Override
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
    }
}
