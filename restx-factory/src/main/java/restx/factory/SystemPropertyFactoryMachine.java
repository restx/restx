package restx.factory;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

public class SystemPropertyFactoryMachine implements FactoryMachine {
    @Override
    public boolean canBuild(Name<?> name) {
        return name.getClazz() == String.class && System.getProperty(name.getName()) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MachineEngine<T> getEngine(final Name<T> name) {
        return new NoDepsMachineEngine<T>(name, priority(), BoundlessComponentBox.FACTORY) {
            @Override
            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                // in case the system property has been nullified, we return an empty string as value, rather than null
                // which would break the component building
                return (T) Objects.firstNonNull(System.getProperty(name.getName()), "");
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
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

    public String toString() {
        return getClass().getSimpleName();
    }
}
