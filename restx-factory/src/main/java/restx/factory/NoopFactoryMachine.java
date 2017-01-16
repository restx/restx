package restx.factory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

/**
 * A machine that doesn't build any component.
 */
public class NoopFactoryMachine implements FactoryMachine {
    public static final FactoryMachine INSTANCE = new NoopFactoryMachine();

    private NoopFactoryMachine() {
    }

    @Override
    public boolean canBuild(Name<?> name) {
        return false;
    }

    @Override
    public <T> MachineEngine<T> getEngine(Name<T> name) {
        throw new UnsupportedOperationException("Noop machine can't build any component");
    }

    @Override
    public <T> Set<Name<T>> nameBuildableComponents(Type componentType) {
        return Collections.emptySet();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String toString() {
        return "NoopFactoryMachine";
    }
}
