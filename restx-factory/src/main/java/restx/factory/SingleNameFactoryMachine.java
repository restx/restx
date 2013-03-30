package restx.factory;

import java.util.Collections;
import java.util.Set;

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
                || (   this.name.getName().equals(name.getName())
                    && name.getClazz().isAssignableFrom(this.name.getClazz())));
    }

    @Override
    public <T> MachineEngine<T> getEngine(Name<T> name) {
        return (MachineEngine<T>) engine;
    }

    @Override
    public Set nameBuildableComponents(Class componentClass) {
        if (componentClass.isAssignableFrom(name.getClazz())) {
            return Collections.singleton(name);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public int priority() {
        return priority;
    }

}
