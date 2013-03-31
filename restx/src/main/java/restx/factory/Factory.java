package restx.factory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MorePreconditions.checkPresent;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:42 PM
 */
public class Factory implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(Factory.class);
    private static final Name<Factory> FACTORY_NAME = Name.of(Factory.class, "FACTORY");
    private static final Comparator<ComponentCustomizer> customizerComparator = new Comparator<ComponentCustomizer>() {
        @Override
        public int compare(ComponentCustomizer o1, ComponentCustomizer o2) {
            return Ordering.natural().compare(o1.priority(), o2.priority());
        }
    };

    public Warehouse getWarehouse() {
        return warehouse;
    }

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

    public static abstract class Query<T> {
        public static <T> Query<T> byName(Name<T> name) {
            return new NameQuery(name);
        }

        public static <T> Query<T> byClass(Class<T> componentClass) {
            return new ClassQuery(componentClass);
        }

        public static Query<Factory> factoryQuery() {
            return new FactoryQuery();
        }

        private final boolean mandatory;
        private final Factory factory;

        protected Query(Factory factory, boolean mandatory) {
            this.mandatory = mandatory;
            this.factory = factory;
        }

        public abstract Query<T> bind(Factory factory);

        protected Factory factory() {
            return Preconditions.checkNotNull(factory,
                    "illegal method call while factory is not bound on query %s", this);
        }

        protected Factory mayGetFactory() {
            return factory;
        }

        public Query<T> mandatory() {
            return setMandatory(true);
        }

        public Query<T> optional() {
            return setMandatory(false);
        }

        abstract Query<T> setMandatory(boolean mandatory);

        public boolean isMandatory() {
            return this.mandatory;
        }

        public abstract boolean isMultiple();
        public final  Optional<NamedComponent<T>> findOne() {
            Optional<NamedComponent<T>> one = doFindOne();
            if (isMandatory()) {
                checkPresent(one,
                       "component satisfying %s not found.\n%s", this, factory);
            }
            return one;
        }
        public final Set<NamedComponent<T>> find() {
            Set<NamedComponent<T>> namedComponents = doFind();
            if (isMandatory() && namedComponents.isEmpty()) {

            }
            return namedComponents;
        }
        public final Set<T> findAsComponents() {
            return Sets.newLinkedHashSet(
                        Iterables.transform(find(), NamedComponent.<T>toComponent()));
        }

        protected abstract Optional<NamedComponent<T>> doFindOne();
        protected abstract Set<NamedComponent<T>> doFind();
    }

    static abstract class MultipleQuery<T> extends Query<T> {
        protected MultipleQuery(Factory factory, boolean mandatory) {
            super(factory, mandatory);
        }

        @Override
        public boolean isMultiple() {
            return true;
        }

        protected Optional<NamedComponent<T>> doFindOne() {
            Set<NamedComponent<T>> components = doFind();
            if (components.isEmpty()) {
                return Optional.absent();
            } else if (components.size() == 1) {
                return Optional.of(components.iterator().next());
            } else {
                throw new IllegalStateException(String.format(
                        "more than one component is available for query %s." +
                                " Please select which one you want with a more specific query." +
                                " Available components are: %s",
                        this, components));
            }
        }
    }

    static abstract class SingleQuery<T> extends Query<T> {
        protected SingleQuery(Factory factory, boolean mandatory) {
            super(factory, mandatory);
        }

        @Override
        public boolean isMultiple() {
            return false;
        }

        protected Set<NamedComponent<T>> doFind() {
            return doFindOne().asSet();
        }
    }

    static class FactoryQuery extends SingleQuery<Factory> {
        FactoryQuery() {
            this(null);
        }

        FactoryQuery(Factory factory) {
            super(factory, true);
        }

        @Override
        public Query<Factory> bind(Factory factory) {
            return new FactoryQuery(factory);
        }

        @Override
        public Query<Factory> setMandatory(boolean mandatory) {
            return this;
        }

        @Override
        protected Optional<NamedComponent<Factory>> doFindOne() {
            return Optional.of(new NamedComponent<Factory>(FACTORY_NAME, factory()));
        }

        @Override
        public String toString() {
            return "FactoryQuery";
        }
    }

    static class NameQuery<T> extends SingleQuery<T> {
        private final Name<T> name;

        NameQuery(Name<T> name) {
            this(null, true, name);
        }

        NameQuery(Factory factory, boolean mandatory, Name<T> name) {
            super(factory, mandatory);
            this.name = name;
        }

        @Override
        public Query<T> bind(Factory factory) {
            return new NameQuery(factory, isMandatory(), getName());
        }

        @Override
        public Query<T> setMandatory(boolean mandatory) {
            return new NameQuery(mayGetFactory(), isMandatory(), getName());
        }

        @Override
        public boolean isMultiple() {
            return false;
        }

        public Name<T> getName() {
            return name;
        }

        @Override
        protected Optional<NamedComponent<T>> doFindOne() {
            Optional<NamedComponent<T>> component = factory().warehouse.checkOut(name);
            if (component.isPresent()) {
                return component;
            }

            for (FactoryMachine machine : factory().machines) {
                Optional<NamedComponent<T>> namedComponent = factory().buildAndStore(name, machine);
                if (namedComponent.isPresent()) {
                    return namedComponent;
                }
            }
            return Optional.absent();
        }

        @Override
        public String toString() {
            return "QueryByName{" +
                    "name=" + name +
                    '}';
        }
    }

    static class ClassQuery<T> extends MultipleQuery<T> {
        private final Class<T> componentClass;

        ClassQuery(Class<T> componentClass) {
            this(null, false, componentClass);
        }

        ClassQuery(Factory factory, boolean mandatory, Class<T> componentClass) {
            super(factory, mandatory);
            this.componentClass = componentClass;
        }

        @Override
        public Query<T> bind(Factory factory) {
            return new ClassQuery(factory, isMandatory(), getComponentClass());
        }

        @Override
        public Query<T> setMandatory(boolean mandatory) {
            return new ClassQuery(mayGetFactory(), isMandatory(), getComponentClass());
        }

        public Class<T> getComponentClass() {
            return componentClass;
        }

        @Override
        protected Set<NamedComponent<T>> doFind() {
            Set<NamedComponent<T>> components = Sets.newLinkedHashSet();
            Set<Name<T>> builtNames = Sets.newHashSet();
            for (FactoryMachine machine : factory().machines) {
                Set<Name<T>> names = machine.nameBuildableComponents(componentClass);
                for (Name<T> name : names) {
                    if (!builtNames.contains(name)) {
                        Optional<NamedComponent<T>> component = factory().warehouse.checkOut(name);
                        if (component.isPresent()) {
                            components.add(component.get());
                            builtNames.add(name);
                        } else {
                            Optional<NamedComponent<T>> namedComponent = factory().buildAndStore(name, machine);
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

        @Override
        public String toString() {
            return "QueryByClass{" +
                    "componentClass=" + componentClass +
                    '}';
        }
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

    public <T> Query<T> queryByName(Name<T> name) {
        return new NameQuery(name).bind(this);
    }

    public <T> Query<T> queryByClass(Class<T> componentClass) {
        return new ClassQuery(componentClass).bind(this);
    }

    public void close() {
        warehouse.close();
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
        if (!machine.canBuild(name)) {
            return Optional.absent();
        }

        MachineEngine<T> engine = machine.getEngine(name);

        BillOfMaterials bom = engine.getBillOfMaterial();
        SatisfiedBOM satisfiedBOM = satisfy(name, bom);

        logger.info("building {} with {}", name, engine);
        Monitor monitor = MonitorFactory.start("<BUILD> " + name.getSimpleName());
        ComponentBox<T> box = engine.newComponent(satisfiedBOM);
        monitor.stop();

        if (!ComponentCustomizerEngine.class.isAssignableFrom(name.getClazz())) {
            // we don't customize customizers themselves to avoid causing a stck overflow
            // it would be possible to introduce that feature, it would require isolating the construction of customizers
            // from their customization. But that's not simple and there is no use case for that so far.
            List<ComponentCustomizer> customizers = Lists.newArrayList();
            for (ComponentCustomizerEngine customizerEngine : customizerEngines()) {
                if (customizerEngine.canCustomize(box.getName())) {
                    customizers.add(customizerEngine.getCustomizer(box.getName()));
                }
            }
            for (ComponentCustomizer customizer : Ordering.from(customizerComparator).sortedCopy(customizers)) {
                Monitor customizeMonitor = MonitorFactory.start("<CUSTOMIZE> " + name.getSimpleName()
                        + " <WITH> " + customizer.getClass().getSimpleName());
                logger.info("customizing {} with {}", name, customizer);
                box = box.customize(customizer);
                customizeMonitor.stop();
            }
        }

        warehouse.checkIn(box, satisfiedBOM, monitor);
        return warehouse.checkOut(box.getName());
    }

    private Set<ComponentCustomizerEngine> customizerEngines() {
        return queryByClass(ComponentCustomizerEngine.class).findAsComponents();
    }

    private SatisfiedBOM satisfy(Name name, BillOfMaterials bom) {
        logger.info("satisfying BOM for {} - {}", name, bom);
        ImmutableMultimap.Builder<Query<?>, NamedComponent<?>> materials = ImmutableMultimap.builder();

        for (Query key : bom.getQueries()) {
            materials.putAll(key, key.bind(this).find());
        }

        return new SatisfiedBOM(bom, materials.build());
    }


}
