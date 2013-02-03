package restx.factory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 7:23 PM
 */
public class DefaultFactoryMachine implements FactoryMachine {
    public static interface SingleComponentSupplier<T> {
        public T newComponent(Factory factory);
    }

    public static class SingleComponentBoxSupplier<T> {
        public static <T> SingleComponentBoxSupplier<T> of(Name<T> name, ComponentBox.BoxFactory boxFactory,
                                                                 SingleComponentSupplier<T> supplier) {
            return new SingleComponentBoxSupplier<>(name, boxFactory, supplier);
        }

        private final Name<T> name;
        private final SingleComponentSupplier<T> supplier;
        private final ComponentBox.BoxFactory boxFactory;

        private SingleComponentBoxSupplier(Name<T> name, ComponentBox.BoxFactory boxFactory,
                                           SingleComponentSupplier<T> supplier) {
            this.name = name;
            this.boxFactory = boxFactory;
            this.supplier = supplier;
        }

        public Name<T> getName() {
            return name;
        }

        public Optional<? extends ComponentBox<T>> newBox(Factory factory) {
            return Optional.of(
                                boxFactory.of(new NamedComponent<>(name, supplier.newComponent(factory))));
        }

        @Override
        public String toString() {
            return "SingleComponentBoxSupplier{" +
                    "name=" + name +
                    ", supplier=" + supplier +
                    ", boxFactory=" + boxFactory +
                    '}';
        }
    }

    private final int priority;
    protected final ImmutableMap<Name<?>, SingleComponentBoxSupplier<?>> suppliers;

    public DefaultFactoryMachine(int priority, SingleComponentBoxSupplier<?>... suppliers) {
        this.priority = priority;
        ImmutableMap.Builder<Name<?>, SingleComponentBoxSupplier<?>> builder = ImmutableMap.builder();
        for (SingleComponentBoxSupplier<?> supplier : suppliers) {
            builder.put(supplier.getName(), supplier);
        }
        this.suppliers = builder.build();
    }

    @Override
    public <T> Optional<? extends ComponentBox<T>> newComponent(Factory factory, Name<T> name) {
        SingleComponentBoxSupplier supplier = suppliers.get(name);
        if (supplier != null) {
            return supplier.newBox(factory);
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Set nameBuildableComponents(Class componentClass) {
        Set names = Sets.newHashSet();
        for (Name name : suppliers.keySet()) {
            if (componentClass.isAssignableFrom(name.getClazz())) {
                names.add(name);
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
                ", suppliers=" + suppliers +
                '}';
    }
}
