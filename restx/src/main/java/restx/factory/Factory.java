package restx.factory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MorePreconditions.checkPresent;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:42 PM
 */
public class Factory {
    private final Logger logger = LoggerFactory.getLogger(Factory.class);

    public static class LocalMachines {
        private static final ThreadLocal<LocalMachines> threadLocals = new ThreadLocal() {
            @Override
            protected LocalMachines initialValue() {
                return new LocalMachines();
            }
        };

        private static ConcurrentMap<String, LocalMachines> contextLocals = new ConcurrentHashMap<>();

        public static LocalMachines threadLocal() {
            return threadLocals.get();
        }

        public static LocalMachines contextLocal(String ctxName) {
            contextLocals.putIfAbsent(ctxName, new LocalMachines());
            return contextLocals.get(ctxName);
        }

        private final List<FactoryMachine> machines = Lists.newArrayList();

        public LocalMachines addMachine(FactoryMachine machine) {
            machines.add(machine);
            return this;
        }

        public void clear() {
            machines.clear();
        }

        ImmutableList<FactoryMachine> get() {
            return ImmutableList.copyOf(machines);
        }
    }

    public static class Builder {
        private List<FactoryMachine> machines = Lists.newArrayList();

        public Builder addFromServiceLoader() {
            Iterables.addAll(machines, ServiceLoader.load(FactoryMachine.class));
            return this;
        }

        public Builder addLocalMachines(LocalMachines localMachines) {
            machines.addAll(localMachines.get());
            return this;
        }

        public Builder addMachine(FactoryMachine machine) {
            machines.add(machine);
            return this;
        }

        public Factory build() {
            return new Factory(machines, new Warehouse());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final ImmutableList<FactoryMachine> machines;
    private final Warehouse warehouse;

    private Factory(List<FactoryMachine> machines, Warehouse warehouse) {
        this.machines = ImmutableList.copyOf(
                Ordering.from(new Comparator<FactoryMachine>() {
                    @Override
                    public int compare(FactoryMachine o1, FactoryMachine o2) {
                        return Integer.compare(o1.priority(), o2.priority());
                    }
                }).sortedCopy(machines));
        this.warehouse = checkNotNull(warehouse);
    }

    public int getNbMachines() {
        return machines.size();
    }

    public synchronized <T> Optional<NamedComponent<T>> getNamedComponent(Name<T> name) {
        Optional<NamedComponent<T>> component = warehouse.checkOut(name);
        if (component.isPresent()) {
            return component;
        }

        for (FactoryMachine machine : machines) {
            Optional<NamedComponent<T>> namedComponent = buildAndStore(name, machine);
            if (namedComponent.isPresent()) {
                return namedComponent;
            }
        }
        return Optional.absent();
    }

    public synchronized <T> Set<NamedComponent<T>> getNamedComponents(Class<T> componentClass) {
        Set<NamedComponent<T>> components = Sets.newLinkedHashSet();
        Set<Name<T>> builtNames = Sets.newHashSet();
        for (FactoryMachine machine : machines) {
            Set<Name<T>> names = machine.nameBuildableComponents(componentClass);
            for (Name<T> name : names) {
                if (!builtNames.contains(name)) {
                    Optional<NamedComponent<T>> component = warehouse.checkOut(name);
                    if (component.isPresent()) {
                        components.add(component.get());
                        builtNames.add(name);
                    } else {
                        Optional<NamedComponent<T>> namedComponent = buildAndStore(name, machine);
                        if (namedComponent.isPresent()) {
                            components.add(namedComponent.get());
                            builtNames.add(name);
                        }
                    }
                }
            }
        }

        return components;
    }

    public <T> Set<T> getComponents(Class<T> componentClass) {
        return Sets.newLinkedHashSet(
                Iterables.transform(getNamedComponents(componentClass), NamedComponent.<T>toComponent()));
    }

    public <T> Optional<NamedComponent<T>> getNamedComponent(Class<T> componentClass) {
        Set<NamedComponent<T>> components = getNamedComponents(componentClass);
        if (components.isEmpty()) {
            return Optional.absent();
        } else if (components.size() == 1) {
            return Optional.of(components.iterator().next());
        } else {
            throw new IllegalStateException(String.format(
                    "more than one component is available for type %s." +
                            " Please select which one you want with a Name." +
                            " Available components are: %s",
                    componentClass.getName(), components));
        }
    }

    public <T> NamedComponent<T> mustGetNamedComponent(Name<T> name) {
        return checkPresent(getNamedComponent(name),
                "component named %s not found.\n%s", name, this);
    }

    public <T> NamedComponent<T> mustGetNamedComponent(Class<T> componentClass) {
        return checkPresent(getNamedComponent(componentClass),
                "component of class %s not found.\n%s", componentClass, this);
    }

    @Override
    public String toString() {
        return  "---------------------------------------------\n" +
                "                 Factory\n" +
                "--> Machines\n" +
                Joiner.on("\n").join(machines) +
                "\n--\n" +
                "--> Warehouse\n" +
                warehouse +
                "\n--\n" +
                "---------------------------------------------";
    }

    private <T> Optional<NamedComponent<T>> buildAndStore(Name<T> name, FactoryMachine machine) {
        Monitor monitor = MonitorFactory.start("BUILD." + name.toString());
        Optional<? extends ComponentBox<T>> box = machine.newComponent(this, name);
        if (box.isPresent()) {
            monitor.stop();
            warehouse.checkIn(box.get());
            return warehouse.checkOut(box.get().getName());
        } else {
            return Optional.absent();
        }
    }


}
