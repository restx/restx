package restx.factory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 7:23 PM
 */
public class DefaultFactoryMachine implements FactoryMachine {

    private final int priority;
    protected final ImmutableMap<Name<?>, MachineEngine<?>> engines;

    public DefaultFactoryMachine(int priority, MachineEngine<?>... engines) {
        this.priority = priority;
        ImmutableMap.Builder<Name<?>, MachineEngine<?>> builder = ImmutableMap.builder();
        for (MachineEngine<?> engine : engines) {
            builder.put(engine.getName(), engine);
        }
        this.engines = builder.build();
    }

    @Override
    public boolean canBuild(Name<?> name) {
        return engines.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MachineEngine<T> getEngine(Name<T> name) {
        return (MachineEngine<T>) engines.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        Set<Name<T>> names = Sets.newHashSet();
        for (Name<?> name : engines.keySet()) {
            if (componentClass.isAssignableFrom(name.getClazz())) {
                names.add((Name<T>) name);
            }
        }
        return names;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public String toString() {
        return "DefaultFactoryMachine{" +
                "priority=" + priority +
                ", engines=" + engines +
                '}';
    }
}
