package restx.factory;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * This will be moved to restx-factory in a later release
 */
@Machine
public class SystemPropertyFactoryMachine implements FactoryMachine {
    @Override
    public boolean canBuild(Name<?> name) {
        return name.getClazz() == String.class && System.getProperty(name.getName()) != null;
    }

    @Override
    public <T> MachineEngine<T> getEngine(final Name<T> name) {
        return new NoDepsMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {
            @Override
            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                return (T) System.getProperty(name.getName());
            }
        };
    }

    @Override
    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        if (componentClass != String.class) {
            return emptySet();
        }
        Set<Name<T>> names = new LinkedHashSet<>();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            names.add((Name<T>) Name.of(String.class, (String) entry.getKey()));
        }
        return names;
    }

    @Override
    public int priority() {
        return -1000;
    }
}
