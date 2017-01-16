package restx.factory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import restx.common.Types;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 7:23 PM
 */
public class SingleNameFactoryMachine<C> implements FactoryMachine {
    private final int priority;
    private final MachineEngine<C> engine;
    private final Name<C> name;

    public SingleNameFactoryMachine(int priority, MachineEngine<C> engine) {
        this.priority = priority;
        this.engine = engine;
        this.name = engine.getName();
    }

    @Override
    public boolean canBuild(Name<?> name) {
        return (this.name.equals(name)
                || (this.name.getName().equals(name.getName())
                && Types.isAssignableFrom(name.getType(), this.name.getType())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MachineEngine<T> getEngine(Name<T> name) {
        return (MachineEngine<T>) engine;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<Name<T>> nameBuildableComponents(Type componentType) {
        if (Types.isAssignableFrom(componentType, name.getType())) {
            return Collections.singleton((Name<T>) name);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public String toString() {
        return "SingleNameFactoryMachine{" +
                "priority=" + priority +
                ", engine=" + engine +
                ", name=" + name +
                '}';
    }
}
