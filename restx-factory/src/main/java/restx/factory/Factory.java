package restx.factory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.MoreObjects;
import restx.common.metrics.api.MetricRegistry;
import restx.common.metrics.api.Monitor;
import restx.common.metrics.api.Timer;
import restx.common.metrics.dummy.DummyMetricRegistry;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static restx.common.MorePreconditions.checkPresent;
import static restx.common.MoreStrings.indent;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:42 PM
 */
public class Factory implements AutoCloseable {

    public static final String METRICS_REGISTRY = "MetricRegistry";
    public static final String HEALTH_CHECK_REGISTRY = "HealthChekcRegistry";

    public static final Comparator<MachineEngine<?>> ENGINE_COMPARATOR = new Comparator<MachineEngine<?>>() {
        @Override
        public int compare(MachineEngine<?> o1, MachineEngine<?> o2) {
            return compareByPriorityAndName(o1.getPriority(), o1.getName(), o2.getPriority(), o2.getName());
        }
    };
    public static final Comparator<NamedComponent<?>> NAMED_COMPONENT_COMPARATOR = new Comparator<NamedComponent<?>>() {
        @Override
        public int compare(NamedComponent<?> o1, NamedComponent<?> o2) {
            return compareByPriorityAndName(o1.getPriority(), o1.getName(), o2.getPriority(), o2.getName());
        }
    };

    private static final String SERVICE_LOADER = "ServiceLoader";
    private static final Logger logger = LoggerFactory.getLogger(Factory.class);
    private static final Name<Factory> FACTORY_NAME = Name.of(Factory.class, "FACTORY");
    private static final Name<MetricRegistry> METRICS_NAME = Name.of(MetricRegistry.class, METRICS_REGISTRY);
    private static final Comparator<ComponentCustomizer> customizerComparator = new Comparator<ComponentCustomizer>() {
        @Override
        public int compare(ComponentCustomizer o1, ComponentCustomizer o2) {
            return o1.priority() - o2.priority();
        }
    };
    private static int compareByPriorityAndName(int p1, Name<?> n1, int p2, Name<?> n2) {
        int priorityComparison = Integer.compare(p1, p2);
        if (priorityComparison == 0) {
            return n1.asId().compareTo(n2.asId());
        } else {
            return priorityComparison;
        }
    }
    private static final AtomicLong ID = new AtomicLong();

    private static final ConcurrentMap<String, Factory> factories = Maps.newConcurrentMap();

    public static Optional<Factory> getFactory(String key) {
        return Optional.fromNullable(factories.get(key));
    }

    public static Factory register(String key, Factory factory) {
        Factory previous = factories.putIfAbsent(key, factory);
        if (previous != null) {
            return previous;
        }
        return factory;
    }

    public static boolean unregister(String key, Factory factory) {
        return factories.remove(key, factory);
    }

    public static Factory newInstance() {
        return Factory.builder().addFromServiceLoader()
                .addLocalMachines(LocalMachines.threadLocal())
                .build();
    }

    /**
     * Returns a default Factory instance, getting componenents from ServiceLoader only.
     *
     * Make sure you never close that instance except on JVM shutdown, it's probably shared among several usages.
     *
     * Prefer using your own Factory with newInstance for instance if you want to have control over its lifecycle.
     *
     * @return the default factory instance.
     */
    public static Factory getInstance() {
        Optional<Factory> factory = getFactory("__DEFAULT__");
        if (factory.isPresent()) {
            return factory.get();
        } else {
            return register("__DEFAULT__", Factory.builder().addFromServiceLoader().build());
        }
    }


    public static class LocalMachines {
        private static final ThreadLocal<String> threadLocals = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return String.format("TL[%s][%03d]", Thread.currentThread().getName(), IDS.incrementAndGet());
            }
        };

        private static final ConcurrentMap<String, LocalMachines> contextLocals = new ConcurrentHashMap<>();
        private static final AtomicLong IDS = new AtomicLong();
        private final String id;

        private LocalMachines(String id) {
            this.id = id;
        }

        /**
         * An alias for threadLocal() which makes the intention of using it to override components clearer.
         *
         * It's usually used like this:
         * <code>
         *     overrideComponents().set("componentName", "componentValue");
         * </code>
         *
         * Note that it doesn't do anything special to actually override components: they will be used only in Factory
         * relying on threadLocal() LocalMachines is built after the call.
         *
         * @return LocalMachines associated with current thread.
         */
        public static LocalMachines overrideComponents() {
            return threadLocal();
        }

        /**
         * Returns a LocalMachines associated with current thread.
         *
         * This is often used when building a Factory, the Factory.newInstance() use it for instance.
         *
         * Thanks to client affinity, it can also be shared between client and server.
         *
         * @return a LocalMachines associated with current thread.
         */
        public static LocalMachines threadLocal() {
            String id = threadLocals.get();
            LocalMachines localMachines = contextLocals.get(id);
            if (localMachines != null) {
                return localMachines;
            }
            LocalMachines m = contextLocals.putIfAbsent(id, localMachines = new LocalMachines(id));
            if (m != null) {
                return m;
            } else {
                return localMachines;
            }
        }

        /**
         * Return LocalMachines associated with another thread, by id.
         *
         * You must know the id of the threadlocal from the other thread to be able to access it.
         *
         * From the other thread do Factory.LocalMachines.threadLocal().getId()
         *
         * @param id the other thread threadLocal() LocalMachines id
         * @return the LocalMachines associated with the other thread, or an empty LocalMachines which is not
         *         automatically registered if none is found.
         */
        public static LocalMachines threadLocalFrom(String id) {
            LocalMachines localMachines = contextLocals.get(id);
            if (localMachines != null) {
                return localMachines;
            }
            return new LocalMachines(id);
        }

        /**
         * Returns a LocalMachines associated with given context name.
         *
         * @param ctxName the context name
         * @return a LocalMachines associated with given context name.
         */
        public static LocalMachines contextLocal(String ctxName) {
            contextLocals.putIfAbsent(ctxName, new LocalMachines(
                    String.format("CTX[%s][$03d]", ctxName, IDS.incrementAndGet())));
            return contextLocals.get(ctxName);
        }

        private final List<FactoryMachine> machines = Lists.newArrayList();

        public LocalMachines addMachine(FactoryMachine machine) {
            machines.add(machine);
            return this;
        }

        public LocalMachines removeMachine(FactoryMachine machine) {
            machines.remove(machine);
            return this;
        }

        public void clear() {
            machines.clear();
        }

        ImmutableList<FactoryMachine> get() {
            return ImmutableList.copyOf(machines);
        }

        public String getId() {
            return id;
        }

        @SuppressWarnings("unchecked")
        public LocalMachines set(String name, Object component) {
            Class aClass = component.getClass();
            set(NamedComponent.of(aClass, name, component));
            return this;
        }
        @SuppressWarnings("unchecked")
        public LocalMachines set(int priority, String name, Object component) {
            Class aClass = component.getClass();
            set(priority, NamedComponent.of(aClass, name, component));
            return this;
        }
        public <T> LocalMachines set(Class<T> clazz, String name, T component) {
            set(NamedComponent.of(clazz, name, component));
            return this;
        }
        public <T> LocalMachines set(int priority, Class<T> clazz, String name, T component) {
            set(priority, NamedComponent.of(clazz, name, component));
            return this;
        }
        public <T> LocalMachines set(NamedComponent<T> namedComponent) {
            set(-1000, namedComponent);
            return this;
        }
        public <T> LocalMachines set(int priority, NamedComponent<T> namedComponent) {
            addMachine(new SingletonFactoryMachine<>(priority, namedComponent));
            return this;
        }
    }

    public static class Builder {
        private boolean usedServiceLoader;
        private Multimap<String, FactoryMachine> machines = ArrayListMultimap.create();
        private List<Warehouse> providers = new ArrayList<>();
        public Builder addFromServiceLoader() {
            machines.putAll(SERVICE_LOADER, FactoryMachinesServiceLoader.getMachines());

            // we have to store separately, in case the list is empty; multimap doesn't keep the key
            usedServiceLoader = true;

            return this;
        }

        public Builder addLocalMachines(LocalMachines localMachines) {
            machines.putAll(localMachines.getId(), localMachines.get());
            return this;
        }

        public Builder addMachine(FactoryMachine machine) {
            machines.put("IndividualMachines", machine);
            return this;
        }

        public Builder addWarehouseProvider(Warehouse warehouse) {
            providers.add(warehouse);
            return this;
        }

        public Builder withMetrics(MetricRegistry metrics) {
            machines.put("IndividualMachines",
                    new SingletonFactoryMachine<>(0, new NamedComponent<>(METRICS_NAME, metrics)));
            return this;
        }

        public Factory build() {
            /*
               Building a Factory is done in several steps:
               1) do a set of rounds until a round is not producing new FactoryMachine
                  --> this allow machines to build other machines, which can benefit from injection
               2) build ComponentCustomizerEngine components, which will be used to customize
                  other components.

               Therefore component customization cannot be used for dependencies of factory machines nor
               component customizers themselves.

               At each step a new factory is created. Indeed factories are immutable. This makes the construction
               slightly less performant but then factory behavior is much more predictable and performant,
               which is better at least in production.
             */
            Factory factory = new Factory(
                    usedServiceLoader, machines, ImmutableList.<ComponentCustomizerEngine>of(),
                    new StdWarehouse(ImmutableList.copyOf(providers)));

            Map<Name<FactoryMachine>, MachineEngine<FactoryMachine>> toBuild = new LinkedHashMap<>();
            ImmutableList<FactoryMachine> factoryMachines = buildFactoryMachines(factory, factory.machines, toBuild);
            while (!factoryMachines.isEmpty()) {
                machines.putAll("FactoryMachines", factoryMachines);
                factory = new Factory(usedServiceLoader, machines,
                        ImmutableList.<ComponentCustomizerEngine>of(), new StdWarehouse());
                factoryMachines = buildFactoryMachines(factory, factoryMachines, toBuild);
            }
            factory = new Factory(usedServiceLoader, machines,
                    buildCustomizerEngines(factory), new StdWarehouse(ImmutableList.copyOf(providers)));
            return factory;
        }

        private ImmutableList<ComponentCustomizerEngine> buildCustomizerEngines(Factory factory) {
            Set<ComponentCustomizerEngine> componentCustomizerEngines = new LinkedHashSet<>();
            for (FactoryMachine machine : factory.machines) {
                Set<Name<ComponentCustomizerEngine>> names =
                        factory.nameBuildableComponents(machine, ComponentCustomizerEngine.class);
                for (Name<ComponentCustomizerEngine> name : names) {
                    Optional<NamedComponent<ComponentCustomizerEngine>> customizer =
                            factory.buildAndStore(Query.byName(name), machine.getEngine(name));
                    componentCustomizerEngines.add(customizer.get().getComponent());
                }
            }
            return ImmutableList.copyOf(componentCustomizerEngines);
        }

        private ImmutableList<FactoryMachine> buildFactoryMachines(
                Factory factory, ImmutableList<FactoryMachine> factoryMachines,
                Map<Name<FactoryMachine>, MachineEngine<FactoryMachine>> toBuild) {
            List<FactoryMachine> machines = new ArrayList<>();
            UnsatisfiedDependencies notSatisfied = UnsatisfiedDependencies.of();
            Map<Name<FactoryMachine>, MachineEngine<FactoryMachine>> moreToBuild = new LinkedHashMap<>();
            for (FactoryMachine machine : factoryMachines) {
                Set<Name<FactoryMachine>> names = factory.nameBuildableComponents(machine, FactoryMachine.class);
                for (Name<FactoryMachine> name : names) {
                    MachineEngine<FactoryMachine> engine = machine.getEngine(name);
                    try {
                        machines.add(factory.buildAndStore(Query.byName(name), engine).get().getComponent());
                    } catch (UnsatisfiedDependenciesException e) {
                        moreToBuild.put(name, engine);
                        notSatisfied = notSatisfied.concat(e.getUnsatisfiedDependencies().prepend(
                                SatisfiedQuery.of(Query.byName(name), name)));
                    }
                }
            }

            for (Map.Entry<Name<FactoryMachine>, MachineEngine<FactoryMachine>> entry : new ArrayList<>(toBuild.entrySet())) {
                try {
                    machines.add(factory.buildAndStore(Query.byName(entry.getKey()), entry.getValue()).get().getComponent());
                    toBuild.remove(entry.getKey());
                } catch (UnsatisfiedDependenciesException e) {
                    notSatisfied = notSatisfied.concat(e.getUnsatisfiedDependencies().prepend(
                            SatisfiedQuery.of(Query.byName(entry.getKey()), entry.getKey())));
                }
            }

            toBuild.putAll(moreToBuild);

            if (!notSatisfied.isEmpty() // some deps were not satisfied
                    && machines.isEmpty() // and we produced no new machines, so there is no chance we satisfy them later
                    ) {
                throw notSatisfied.raise();
            }

            return ImmutableList.copyOf(machines);
        }


    }

    public static Builder builder() {
        return new Builder();
    }

    public static abstract class Query<T> {
        public static <T> Query<T> byName(Name<T> name) {
            return new NameQuery<>(name);
        }

        public static <T> Query<T> byClass(Class<T> componentClass) {
            return new ClassQuery<>(componentClass);
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

        public abstract Class<T> getComponentClass();

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
        public final Optional<NamedComponent<T>> findOne() {
            return doFindOne();
        }
        public final Optional<T> findOneAsComponent() {
            Optional<NamedComponent<T>> namedComponent = findOne();
            if(namedComponent.isPresent()){
                return Optional.of(namedComponent.get().getComponent());
            } else {
                return Optional.absent();
            }
        }
        public final Set<NamedComponent<T>> find() {
            return doFind();
        }
        public final Set<T> findAsComponents() {
            return ImmutableSet.copyOf(
                        Iterables.transform(find(), NamedComponent.<T>toComponent()));
        }
        public abstract Set<Name<T>> findNames();

        protected abstract Optional<NamedComponent<T>> doFindOne();
        protected abstract Set<NamedComponent<T>> doFind();

        public void checkSatisfy() {
            if (!isMandatory()) {
                return;
            }
            Set<Name<T>> names = findNames();
            if (names.isEmpty()) {
                throw UnsatisfiedDependency.on(this).raise();
            }
            Factory f = factory();
            for (Name<T> name : names) {
                f.checkSatisfy(name);
            }
        }
    }

    public static abstract class MultipleQuery<T> extends Query<T> {
        protected MultipleQuery(Factory factory, boolean mandatory) {
            super(factory, mandatory);
        }

        @Override
        public boolean isMultiple() {
            return true;
        }

        @Override
        protected Optional<NamedComponent<T>> doFindOne() {
            Set<NamedComponent<T>> components = doFind();
            if (components.isEmpty()) {
                return Optional.absent();
            } else if (components.size() == 1) {
                return Optional.of(components.iterator().next());
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("more than one component is available for query ").append(this).append(".\n")
                        .append(" Please select which one you want with a more specific query,\n")
                        .append(" or by deactivating one of the available components.\n")
                        .append(" Available components:\n");
                for (NamedComponent<T> component : components) {
                    sb.append(" - ").append(component).append("\n")
                            .append("         [Activation key: '")
                            .append(activationKey(component.getName().getClazz(), component.getName().getName()))
                            .append("']\n");
                }

                throw UnsatisfiedDependency.on(this).causedBy(sb.toString()).raise();
            }
        }
    }

    public static abstract class SingleQuery<T> extends Query<T> {
        protected SingleQuery(Factory factory, boolean mandatory) {
            super(factory, mandatory);
        }

        @Override
        public boolean isMultiple() {
            return false;
        }

        @Override
        protected Set<NamedComponent<T>> doFind() {
            return doFindOne().asSet();
        }
    }

    public static class FactoryQuery extends SingleQuery<Factory> {
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
            return Optional.of(new NamedComponent<>(FACTORY_NAME, factory()));
        }

        @Override
        public Set<Name<Factory>> findNames() {
            return Collections.singleton(FACTORY_NAME);
        }

        @Override
        public Class<Factory> getComponentClass() {
            return Factory.class;
        }

        @Override
        public String toString() {
            return "FactoryQuery";
        }
    }

    public static class NameQuery<T> extends SingleQuery<T> {
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
            return new NameQuery<>(factory, isMandatory(), getName());
        }

        @Override
        public Query<T> setMandatory(boolean mandatory) {
            return new NameQuery<>(mayGetFactory(), mandatory, getName());
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

            for (MachineEngine<T> engine : factory().findAllEnginesFor(name)) {
                Optional<NamedComponent<T>> namedComponent = factory().buildAndStore(this, engine);
                if (namedComponent.isPresent()) {
                    return namedComponent;
                }
            }
            return Optional.absent();
        }

        @Override
        public Set<Name<T>> findNames() {
            return factory().checkActive(name) ? Collections.singleton(name) : Collections.<Name<T>>emptySet();
        }

        @Override
        public Class<T> getComponentClass() {
            return name.getClazz();
        }

        @Override
        public String toString() {
            return "QueryByName{" +
                    "name=" + name +
                    '}';
        }
    }

    public static class ClassQuery<T> extends MultipleQuery<T> {
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
            return new ClassQuery<>(factory, isMandatory(), getComponentClass());
        }

        @Override
        public Query<T> setMandatory(boolean mandatory) {
            return new ClassQuery<>(mayGetFactory(), mandatory, getComponentClass());
        }

        @Override
        public Class<T> getComponentClass() {
            return componentClass;
        }

        @Override
        protected Set<NamedComponent<T>> doFind() {
            Set<NamedComponent<T>> components = Sets.newTreeSet(NAMED_COMPONENT_COMPARATOR);
            Factory factory = factory();
            for (Name<T> tName : factory.collectAllBuildableNames(componentClass)) {
                components.addAll(factory.queryByName(tName).optional().find());
            }

            return components;
        }

        @Override
        public Set<Name<T>> findNames() {
            return factory().collectAllBuildableNames(componentClass);
        }

        @Override
        public String toString() {
            return "QueryByClass{" +
                    "componentClass=" + MoreObjects.toString(componentClass) +
                    '}';
        }
    }

    public static <T> String activationKey(Class<T> aClass, String name) {
        return "restx.activation::" + aClass.getName() + "::" + name;
    }

    private final class CanBuildPredicate implements Predicate<FactoryMachine> {
        private final Name<?> name;

        private CanBuildPredicate(Name<?> name) {
            this.name = name;
        }

        @Override
        public boolean apply(FactoryMachine input) {
            return input != null && input.canBuild(name);
        }
    }

    private final boolean usedServiceLoader;
    private final ImmutableList<FactoryMachine> machines;
    private final ImmutableMultimap<String, FactoryMachine> machinesByBuilder;
    private final Warehouse warehouse;
    private final ImmutableList<ComponentCustomizerEngine> customizerEngines;
    private final String id;
    private final Object dumper = new Object() { public String toString() { return Factory.this.dump(); } };

    private final Set<Name> deactivatedComponents = new CopyOnWriteArraySet<>();
    private final Set<Name> activatedComponents = new CopyOnWriteArraySet<>();

    private MetricRegistry metrics;

    private Factory(boolean usedServiceLoader, Multimap<String, FactoryMachine> machines,
                    ImmutableList<ComponentCustomizerEngine> customizerEngines, Warehouse warehouse) {
        this.usedServiceLoader = usedServiceLoader;
        this.customizerEngines = customizerEngines;

        ImmutableMultimap.Builder<String, FactoryMachine> machineBuilder = ImmutableMultimap.<String, FactoryMachine>builder()
                .put("FactoryMachine", new SingletonFactoryMachine<>(10000, new NamedComponent<>(FACTORY_NAME, this)))

                // define a Machine to provide a default provider for MetricRegistry
                // this won't be used if a MetricsRegistry is provided through a higher priority machine
                .put("MetricRegistryMachine", new SingleNameFactoryMachine<>(10000,
                        new NoDepsMachineEngine<MetricRegistry>(METRICS_NAME, 10000, BoundlessComponentBox.FACTORY) {
                            @Override
                            protected MetricRegistry doNewComponent(SatisfiedBOM satisfiedBOM) {
                                return new DummyMetricRegistry();
                            }
                        }))

                ;

        if (!warehouse.getProviders().isEmpty()) {
            machineBuilder
                    .put("WarehouseProvidersMachine", new WarehouseProvidersMachine(warehouse.getProviders()));
        }

        machinesByBuilder = machineBuilder
                .putAll(machines)
                .build();

        this.machines = ImmutableList.copyOf(
                Ordering.from(new Comparator<FactoryMachine>() {
                    @Override
                    public int compare(FactoryMachine o1, FactoryMachine o2) {
                        return Integer.compare(o1.priority(), o2.priority());
                    }
                }).sortedCopy(machinesByBuilder.values()));
        this.id = String.format("%03d-%s(%d)", ID.incrementAndGet(), warehouse.getId(), machinesByBuilder.size());
        this.warehouse = checkNotNull(warehouse);

        this.metrics = new DummyMetricRegistry(); // give a value so that we can call getComponent which uses metrics to trace
                                             // the MetricRegistry building itself
        this.metrics = getComponent(MetricRegistry.class);
    }

    public Factory concat(FactoryMachine machine) {
        Multimap<String, FactoryMachine> machines = ArrayListMultimap.create();
        machines.putAll(machinesByBuilder);
        machines.removeAll("FactoryMachine");
        machines.removeAll("MetricRegistryMachine");
        machines.removeAll("WarehouseProvidersMachine");
        machines.put("IndividualMachines", machine);
        return new Factory(usedServiceLoader, machines, customizerEngines,
                new StdWarehouse(warehouse.getProviders()));
    }

    public String getId() {
        return id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public int getNbMachines() {
        return machines.size();
    }

    public <T> Query<T> queryByName(Name<T> name) {
        return new NameQuery<>(name).bind(this);
    }

    public <T> Query<T> queryByClass(Class<T> componentClass) {
        return new ClassQuery<>(componentClass).bind(this);
    }

    /**
     * Builds a component by class.
     *
     * This is a shortcut for queryByClass(cls).mandatory().findOneAsComponent().get()
     *
     * Therefore it raises an exception if no component of this class is found or if several one match.
     *
     * @param componentClass
     * @param <T>
     * @return
     */
    public <T> T getComponent(Class<T> componentClass) {
        return checkPresent(queryByClass(componentClass).mandatory().findOneAsComponent(),
                "component of class %s not found", componentClass);
    }

    /**
     * Builds a component by name.
     *
     * This is a shortcut for queryByName(name).mandatory().findOneAsComponent().get()
     *
     * Therefore it raises an exception if no component of this name is found.
     *
     * @param componentName
     * @param <T>
     * @return
     */
    public <T> T getComponent(Name<T> componentName) {
        return checkPresent(queryByName(componentName).mandatory().findOneAsComponent(),
                "component of name %s not found", componentName);
    }

    /**
     * Builds and return all the component of given class.
     *
     * This is a shortcut for queryByClass(componentClass).findAsComponents()
     *
     * @param componentClass the class of the components to build and return
     * @param <T> the type of components
     * @return the set of components of given class
     */
    public <T> Set<T> getComponents(Class<T> componentClass) {
        return queryByClass(componentClass).findAsComponents();
    }

    /**
     * Returns current factory, used only for fluent API to have more readable code
     * like factory.start().and().prepare();
     *
     * @return current factory
     */
    public Factory and() {
        return this;
    }

    /**
     * Starts all the AutoStartable components of this factory.
     */
    public Factory start() {
        for (AutoStartable startable : queryByClass(AutoStartable.class).findAsComponents()) {
            startable.start();
        }
        return this;
    }

    /**
     * Prepares all the AutoPreparable components of this factory.
     */
    public Factory prepare() {
        for (AutoPreparable preparable : queryByClass(AutoPreparable.class).findAsComponents()) {
            preparable.prepare();
        }
        return this;
    }

    public void close() {
        warehouse.close();
    }

    @Override
    public String toString() {
        return  "Factory[" + id + "]";
    }

    /**
     * Returns an object which toString method dumps the factory.
     *
     * This is useful in logger or check messages, to prevent actually calling dump() if log is disabled
     * or check does not raise exception.
     *
     * @return a dumper for the factory.
     */
    public Object dumper() {
        return dumper;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder()
                .append("---------------------------------------------\n")
                .append("             Factory ").append(id).append("\n");

        sb.append("--> Machines by priority\n  ");
        Joiner.on("\n  ").appendTo(sb, machines);
        sb.append("\n--\n");

        sb.append("--> Machines by builder\n");
        for (String builder : machinesByBuilder.keySet()) {
            ImmutableCollection<FactoryMachine> machinesForBuilder = machinesByBuilder.get(builder);
            sb.append("  = ").append(builder).append("(").append(machinesForBuilder.size()).append(" machines) =\n");
            for (FactoryMachine machine : machinesForBuilder) {
                sb.append("     ").append(machine).append("\n");
            }
        }
        sb.append("--\n");

        dumpBuidableComponents(sb);
        dumpDeactivatedComponents(sb);

        Set<String> undeclaredMachines = findUndeclaredMachines();
        if (!undeclaredMachines.isEmpty()) {
            sb.append("--> WARNING: classes annotated with @Machine were found in classpath\n")
              .append("             but not in service declaration files `META-INF/services/restx.factory.FactoryMachine`.\n")
              .append("             Do a clean build and check your annotation processing.\n")
              .append("             List of missing machines:\n");
            for (String undeclaredMachine : undeclaredMachines) {
                sb.append("  ").append(undeclaredMachine).append("\n");
            }
            sb.append("\n--\n");
        }

        sb.append("--> Warehouse\n  ")
            .append(warehouse)
            .append("\n--\n");

        sb.append("---------------------------------------------");
        return sb.toString();
    }

    private void dumpDeactivatedComponents(StringBuilder sb) {
        sb.append("--> Deactivated Components\n");
        Joiner.on("\n\t").appendTo(sb, deactivatedComponents);
        sb.append("--\n");
    }

    private void dumpBuidableComponents(StringBuilder sb) {
        sb.append("--> Buildable Components\n");
        Iterable<Name<Object>> buildableNames = collectAllBuildableNames(Object.class);
        for (Name<?> buildableName : buildableNames) {
            sb.append("   ").append(buildableName).append("\n");
            List<FactoryMachine> allMachinesFor = Lists.newArrayList(findAllMachinesFor(buildableName));
            if (allMachinesFor.isEmpty()) {
                sb.append("      ERROR: machine ").append(findAllMachinesListing(buildableName))
                        .append("\n       lists this name in nameBuildableComponents() ")
                        .append("but doesn't properly implement canBuild()\n");
            } else {
                FactoryMachine machineFor = allMachinesFor.get(0);
                MachineEngine<?> engine = machineFor.getEngine(buildableName);
                sb.append("      BUILD BY: ").append(engine).append("\n");
                if (allMachinesFor.size() > 1) {
                    sb.append("      OVERRIDING:\n");
                    for (FactoryMachine machine : allMachinesFor.subList(1, allMachinesFor.size())) {
                        sb.append("         ").append(machine).append("\n");
                    }
                }

                ImmutableSet<Query<?>> bomQueries = engine.getBillOfMaterial().getQueries();
                if (!bomQueries.isEmpty()) {
                    sb.append("      BOM:\n");
                    for (Query<?> query : bomQueries) {
                        query = query.bind(this);
                        sb.append("        - ").append(query).append("\n");
                        try {
                            query.checkSatisfy();
                            for (Name<?> name : query.findNames()) {
                                sb.append("          -> ").append(name).append("\n");
                            }
                        } catch (UnsatisfiedDependenciesException ex) {
                            sb.append("          ERROR: CAN'T BE SATISFIED: ").append(ex.getMessage()).append("\n");
                        }
                    }
                }
            }
        }
        sb.append("--\n");
    }

    /**
     * Look for classes with @Machine annotation which are not part of factory, if it has been loaded with ServiceLoader.
     *
     * It may happen that annotation processing has not generated the META-INF/services/restx.factory.FactoryMachine
     * properly, am be due to failed incremental compilation.
     */
    private Set<String> findUndeclaredMachines() {
        if (!usedServiceLoader) {
            return Collections.emptySet();
        }
        Set<Class<?>> annotatedMachines = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(""))
                .setScanners(new TypeAnnotationsScanner())
                .build()
                .getTypesAnnotatedWith(Machine.class);

        Set<String> undeclared = Sets.newLinkedHashSet();
        for (Class<?> annotatedMachine : annotatedMachines) {
            boolean found = false;
            for (FactoryMachine machine : machines) {
                if (annotatedMachine.isAssignableFrom(machine.getClass())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                undeclared.add(annotatedMachine.getName());
            }
        }
        return undeclared;
    }

    private List<FactoryMachine> findAllMachinesListing(Name<?> name) {
        List<FactoryMachine> machinesFor = Lists.newArrayList();
        for (FactoryMachine machine : machines) {
            if (nameBuildableComponents(machine, Object.class).contains(name)) {
                machinesFor.add(machine);
            }
        }
        return machinesFor;
    }

    private Iterable<FactoryMachine> findAllMachinesFor(Name<?> name) {
        if (!checkActive(name)) {
            return Collections.emptyList();
        }
        return Iterables.filter(machines, new CanBuildPredicate(name));
    }

    private <T> Iterable<MachineEngine<T>> findAllEnginesFor(final Name<T> name) {
        if (!checkActive(name)) {
            return Collections.emptyList();
        }
        return Ordering.from(ENGINE_COMPARATOR).sortedCopy(
                Iterables.transform(
                    Iterables.filter(machines, new CanBuildPredicate(name)),
                new Function<FactoryMachine, MachineEngine<T>>() {
                    @Override
                    public MachineEngine<T> apply(FactoryMachine input) {
                        return input.getEngine(name);
                    }
                }
        ));
    }

    private <T> Optional<MachineEngine<T>> findMachineEngineFor(Name<T> name) {
        return Optional.fromNullable(Iterables.getFirst(findAllEnginesFor(name), null));
    }

    private <T> MachineEngine<T> getMachineEngineFor(Name<T> name) {
        Optional<MachineEngine<T>> engineFor = findMachineEngineFor(name);
        if (!engineFor.isPresent()) {
            throw UnsatisfiedDependency.on(Query.byName(name)).causedBy(machineNotFoundMessage(name)).raise();
        }

        return engineFor.get();
    }

    private <T> Set<Name<T>> collectAllBuildableNames(Class<T> componentClass) {
        Set<Name<T>> buildableNames = Sets.newLinkedHashSet();
        for (FactoryMachine machine : machines) {
            buildableNames.addAll(nameBuildableComponents(machine, componentClass));
        }
        return buildableNames;
    }

    private <T> Optional<NamedComponent<T>> buildAndStore(Query<T> query, MachineEngine<T> engine) {
        Name<T> name = engine.getName();
        if (!checkActive(name)) {
            return Optional.absent();
        }

        BuildingBox<T> buildingBox = new BuildingBox<>(
                ImmutableList.<SatisfiedQuery<?>>of(SatisfiedQuery.of(query, name)), engine);
        Deque<BuildingBox<?>> dependencies = buildBuildingBoxesClosure(buildingBox);

        logger.debug("{} - dependencies closure for {} is: {}", id, name, dependencies);
        satisfyBoms(dependencies);

        return buildAndStore(buildingBox);
    }

    private Deque<BuildingBox<?>> buildBuildingBoxesClosure(BuildingBox<?> buildingBox) {
        // first we traverse the dependencies graph of this buildingbox, and create a building box for each node
        Deque<BuildingBox<?>> dependenciesWithNoIncomingEdges = new LinkedList<>();
        Map<Name<?>, BuildingBox<?>> dependenciesByName = new HashMap<>();
        Queue<BuildingBox<?>> dependenciesToSatisfy = new LinkedList<BuildingBox<?>>(asList(buildingBox));
        while (!dependenciesToSatisfy.isEmpty()) {
            BuildingBox buildingBox1 = dependenciesToSatisfy.poll();

            ImmutableSet<Query<?>> queries = buildingBox1.engine.getBillOfMaterial().getQueries();
            for (Query<?> query : queries) {
                buildBuildingBoxClosureForQuery(dependenciesByName, dependenciesToSatisfy, buildingBox1, query);
            }

            if (buildingBox1.names.isEmpty()) {
                dependenciesWithNoIncomingEdges.add(buildingBox1);
            }
        }

        // then we do a topological sort of this graph - see http://en.wikipedia.org/wiki/Topological_sorting
        Deque<BuildingBox<?>> dependencies = new ArrayDeque<>();
        while (!dependenciesWithNoIncomingEdges.isEmpty()) {
            BuildingBox n = dependenciesWithNoIncomingEdges.removeFirst();
            dependencies.addLast(n);

            while (!n.predecessorsToSort.isEmpty()) {
                BuildingBox m = (Factory.BuildingBox) n.predecessorsToSort.removeFirst();
                m.depsToSort.remove(n);
                if (m.depsToSort.isEmpty()) {
                    dependenciesWithNoIncomingEdges.add(m);
                }
            }
        }

        return dependencies;
    }

    private <D> void buildBuildingBoxClosureForQuery(
            Map<Name<?>, BuildingBox<?>> dependenciesByName,
            Queue<BuildingBox<?>> dependenciesToSatisfy, BuildingBox<?> buildingBox, Query<D> query) {
        Set<? extends Name<D>> names = query.bind(this).findNames();
        if (names.isEmpty() && query.isMandatory()) {
            Set<Name> similarNames = findSimilarNamesByNamedType(query.getComponentClass());
            if (similarNames.isEmpty()) {
                throw UnsatisfiedDependency.on(buildingBox.hierarchy, query).raise();
            } else {
                throw UnsatisfiedDependency.on(
                        buildingBox.hierarchy, query, machineNotFoundMessage(query, similarNames)).raise();
            }
        }
        for (Name<D> n : names) {
            BuildingBox<D> buildingBox2 = getDependencyBuildingBox(dependenciesByName, n);
            if (buildingBox2 != null) {
                // already in dependencies, but we have to add it to box dependencies
                buildingBox.addName(query, n, buildingBox2);
            } else {
                Optional<MachineEngine<D>> machineFor = findMachineEngineFor(n);
                if (!machineFor.isPresent()) {
                    if (query.isMandatory() && names.size() == 1) {
                        throw UnsatisfiedDependency.on(
                                buildingBox.hierarchy, query, machineNotFoundMessage(n)).raise();
                    }
                } else {
                    buildingBox2 = new BuildingBox<>(
                            ImmutableList.<SatisfiedQuery<?>>builder()
                                    .addAll(buildingBox.hierarchy)
                                    .add(SatisfiedQuery.of(query, n)).build(),
                            machineFor.get());
                    dependenciesToSatisfy.add(buildingBox2);
                    buildingBox.addName(query, n, buildingBox2);
                    dependenciesByName.put(n, buildingBox2);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D> BuildingBox<D> getDependencyBuildingBox(Map<Name<?>, BuildingBox<?>> dependenciesByName, Name<D> n) {
        return (BuildingBox<D>) dependenciesByName.get(n);
    }

    private Set<Name> findSimilarNamesByNamedType(Class componentClass) {
        Set<Name> names = new LinkedHashSet<>();
        for (Name<?> name : warehouse.listNames()) {
            if (name.getName().equals(componentClass.getName())) {
                names.add(name);
            }
        }
        return names;
    }

    private void satisfyBoms(Deque<BuildingBox<?>> dependencies) {
        for (BuildingBox<?> buildingBox = dependencies.pollFirst(); buildingBox != null; buildingBox = dependencies.pollFirst()) {
            if (buildingBox.engine.getBillOfMaterial().getQueries().isEmpty()) {
                buildingBox.satisfiedBOM = new SatisfiedBOM(buildingBox.engine.getBillOfMaterial(),
                        ImmutableMultimap.<Query<?>, NamedComponent<?>>of());
            } else {
                ImmutableMultimap.Builder<Query<?>, NamedComponent<?>> materials = ImmutableListMultimap.builder();
                for (Query<?> key : buildingBox.engine.getBillOfMaterial().getQueries()) {
                    Collection<Name<?>> names = buildingBox.names.get(key);
                    Set<NamedComponent<?>> components = Sets.newTreeSet(NAMED_COMPONENT_COMPARATOR);
                    for (Name<?> name : names) {
                        uncheckedAddIfPresent(components, buildAndStore(getDependencyBuildingBox(buildingBox.deps, name)));
                    }
                    materials.putAll(key, components);
                }
                buildingBox.satisfiedBOM = new SatisfiedBOM(buildingBox.engine.getBillOfMaterial(), materials.build());
            }
        }
    }

    private <T> Optional<NamedComponent<T>> buildAndStore(BuildingBox<T> buildingBox) {
        Name<T> name = buildingBox.engine.getName();
        if (buildingBox == null) {
            throw new IllegalStateException("problem with dependency resolution" +
                    " order no building box for " + name);
        }
        if (buildingBox.component != null) {
            return Optional.of(buildingBox.component);
        }
        Optional<NamedComponent<T>> namedComponent = warehouse.checkOut(name);
        if (namedComponent.isPresent()) {
            buildingBox.component = namedComponent.get();
            return namedComponent;
        }

        SatisfiedBOM satisfiedBOM = buildingBox.satisfiedBOM;
        if (satisfiedBOM == null) {
            if(buildingBox.depsToSort != null && !buildingBox.depsToSort.isEmpty()) {
                StringBuilder circularDependencyLog = new StringBuilder();
                for (BuildingBox<?> boxToSort : buildingBox.depsToSort) {
                    buildCircularDependencyLog(circularDependencyLog, boxToSort, ImmutableSet.<Name>of());
                }
                logger.error("Circular dependency detected : \n{}\n" +
                        "Please, fix this as current RestX DI can't handle cycles.", circularDependencyLog);
                throw new IllegalStateException("Circular dependency detected : \n" +
                        circularDependencyLog + "\nPlease, fix this as current RestX DI can't handle cycles.");
            } else {
                throw new IllegalStateException("problem with dependency resolution" +
                        " order " + buildingBox.engine.getBillOfMaterial() + " for " + name + " not yet satisfied");
            }
        }

        namedComponent = buildAndStore(name, buildingBox.engine, satisfiedBOM);
        // this may be absent if engine creates an absent component
        if (namedComponent.isPresent()) {
            buildingBox.component = namedComponent.get();
        }
        return namedComponent;
    }

    private void buildCircularDependencyLog(StringBuilder circularDependencyLog, BuildingBox buildingBox, ImmutableSet<Name> alreadyDisplayedComponents) {
        StringBuilder indentation = new StringBuilder();
        for(int i=0; i<alreadyDisplayedComponents.size(); i++) {
            indentation.append("  ");
        }

        Iterator buildingBoxToSortIter = buildingBox.depsToSort.iterator();
        while(buildingBoxToSortIter.hasNext()) {
            BuildingBox<?> buildingBoxToSort = (BuildingBox<?>) buildingBoxToSortIter.next();
            circularDependencyLog.append(indentation.toString()).append("-> ").append(buildingBox.engine.getName()).append("\n");
            if(!alreadyDisplayedComponents.contains(buildingBox.engine.getName())) {
                buildCircularDependencyLog(
                        circularDependencyLog, buildingBoxToSort,
                        ImmutableSet.<Name>builder().addAll(alreadyDisplayedComponents).add(buildingBox.engine.getName()).build()
                );
            }
        }
    }

    private <T> Optional<NamedComponent<T>> buildAndStore(Name<T> name, MachineEngine<T> engine, SatisfiedBOM satisfiedBOM) {
        logger.debug("{} - building {} with {} / {}", id, name, engine, satisfiedBOM);
        Timer timer = metrics.timer("<BUILD> " + name.getSimpleName());
        Monitor monitor = timer.time();
        ComponentBox<T> box;
        try {
            box = engine.newComponent(satisfiedBOM);
        } finally {
            monitor.stop();
        }

        if (box instanceof BoundlessComponentBox && box.pick().isPresent()
                && box.pick().get().getComponent() == this) {
            // do not store nor customize the factory itself
            // to prevent stack overflow on close
            return box.pick();
        }

        List<ComponentCustomizer<T>> customizers = Lists.newArrayList();
        for (ComponentCustomizerEngine customizerEngine : customizerEngines()) {
            if (customizerEngine.canCustomize(box.getName())) {
                customizers.add(customizerEngine.getCustomizer(box.getName()));
            }
        }
        for (ComponentCustomizer<T> customizer : Ordering.from(customizerComparator).sortedCopy(customizers)) {
            monitor = metrics.timer("<CUSTOMIZE> " + name.getSimpleName()
                    + " <WITH> " + customizer.getClass().getSimpleName()).time();
            try {
                logger.debug("{} - customizing {} with {}", id, name, customizer);
                box = box.customize(customizer);
            } finally {
                monitor.stop();
            }
        }

        warehouse.checkIn(box, satisfiedBOM);
        return warehouse.checkOut(box.getName());
    }

    private Iterable<ComponentCustomizerEngine> customizerEngines() {
        return customizerEngines;
    }

    private <T> void checkSatisfy(Name<T> name) {
        BillOfMaterials billOfMaterial = getBillOfMaterialsFor(name);
        UnsatisfiedDependencies notSatisfied = UnsatisfiedDependencies.of();
        for (Query<?> query : billOfMaterial.getQueries()) {
            try {
                query.bind(this).checkSatisfy();
            } catch (UnsatisfiedDependenciesException e) {
                notSatisfied = notSatisfied.concat(e.getUnsatisfiedDependencies().prepend(SatisfiedQuery.of(Query.byName(name), name)));
            }
        }
        if (!notSatisfied.isEmpty()) {
            throw notSatisfied.raise();
        }
    }

    private <T> BillOfMaterials getBillOfMaterialsFor(Name<T> name) {
        return getMachineEngineFor(name).getBillOfMaterial();
    }

    private <T> Set<Name<T>> nameBuildableComponents(FactoryMachine machine, Class<T> componentClass) {
        Set<Name<T>> buildableComponents = new LinkedHashSet<>();
        for (Name<T> tName : machine.nameBuildableComponents(componentClass)) {
            if (checkActive(tName)) {
                buildableComponents.add(tName);
            }
        }
        return buildableComponents;
    }

    private <T> boolean canBuild(FactoryMachine machine, Name<T> name) {
        return checkActive(name) && machine.canBuild(name);
    }

    private <T> boolean checkActive(Name<T> name) {
        if (name.getClazz() == String.class && name.getName().startsWith("restx.activation::")) {
            // can't deactivate activation keys themselves
            return true;
        }

        // check 'cache' for perf reasons - core Factory being immutable the 'cache' never needs to be invalidated
        if (deactivatedComponents.contains(name)) {
            return false;
        }
        if (activatedComponents.contains(name)) {
            return true;
        }

        // actual check
        Class<?> aClass = name.getClazz();
        while (aClass != null) {
            if ("false".equals(queryByName(Name.of(String.class, activationKey(aClass, name.getName())))
                    .findOneAsComponent().or("true"))) {
                deactivatedComponents.add(name);
                return false;
            }
            aClass = aClass.getSuperclass();
        }
        activatedComponents.add(name);
        return true;
    }

    private <T> String machineNotFoundMessage(Name<T> name) {
        Set<Name<T>> similarNames = queryByClass(name.getClazz()).findNames();
        return machineNotFoundMessage(name, similarNames);
    }

    private String machineNotFoundMessage(Object what, Set similarNames) {
        return what + " can't be satisfied in " + id + ": no machine found to build it." +
                (similarNames.isEmpty() ? ""
                        : " similar components found: " + Joiner.on(", ").join(similarNames));
    }


    @SuppressWarnings("unchecked")
    private void uncheckedAddIfPresent(Set components, Optional c) {
        if (c.isPresent()) {
            components.add(c.get());
        }
    }

    public static class UnsatisfiedDependency {
        public static <T> UnsatisfiedDependency on(Query<T> unsatisfied) {
            return new UnsatisfiedDependency(ImmutableList.<SatisfiedQuery<?>>of(),
                    unsatisfied, String.format("component satisfying %s not found.", unsatisfied));
        }
        public static UnsatisfiedDependency on(ImmutableList<SatisfiedQuery<?>> path, Query<?> unsatisfied) {
            return on(path, unsatisfied, String.format("component satisfying %s not found.", unsatisfied));
        }

        public static UnsatisfiedDependency on(ImmutableList<SatisfiedQuery<?>> path, Query<?> unsatisfied, String rootCause) {
            StringBuilder sb = new StringBuilder("\n  ");
            String indent = "    ";
            for (SatisfiedQuery<?> query : path) {
                sb.append(query.getQuery()).append("\n")
                        .append(indent).append("|       \\__=> ").append(query.getName()).append("\n")
                        .append(indent).append("|\n")
                        .append(indent).append("+-> ")
                ;
                indent += "  ";
            }
            sb.append(unsatisfied).append("\n")
                    .append(indent).append("    |\n")
                    .append(indent).append("    +--: ");
            sb.append(rootCause).append("\n");

            return new UnsatisfiedDependency(path, unsatisfied, sb.toString());
        }

        private final ImmutableList<SatisfiedQuery<?>> path;
        private final Query<?> unsatisfied;
        private final String cause;

        private UnsatisfiedDependency(ImmutableList<SatisfiedQuery<?>> path, Query<?> unsatisfied, String cause) {
            this.path = path;
            this.unsatisfied = unsatisfied;
            this.cause = cause;
        }

        public ImmutableList<SatisfiedQuery<?>> getPath() {
            return path;
        }

        public Query<?> getUnsatisfied() {
            return unsatisfied;
        }

        public String getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return cause;
        }

        public UnsatisfiedDependency causedBy(String cause) {
            return new UnsatisfiedDependency(path, unsatisfied, cause);
        }

        public UnsatisfiedDependenciesException raise() {
            return new UnsatisfiedDependenciesException(UnsatisfiedDependencies.of(this));
        }

        public <T> UnsatisfiedDependency prepend(SatisfiedQuery<T> query) {
            return new UnsatisfiedDependency(ImmutableList.<SatisfiedQuery<?>>builder()
                    .add(query).addAll(path).build(), unsatisfied, query + indent("\n-> " + cause, 2));
        }
    }

    public static class UnsatisfiedDependencies {
        public static UnsatisfiedDependencies of() {
            return new UnsatisfiedDependencies(ImmutableList.<UnsatisfiedDependency>of());
        }

        public static UnsatisfiedDependencies of(UnsatisfiedDependency unsatisfiedDependency) {
            return new UnsatisfiedDependencies(ImmutableList.of(unsatisfiedDependency));
        }

        private final ImmutableList<UnsatisfiedDependency> unsatisfiedDependencies;

        private UnsatisfiedDependencies(ImmutableList<UnsatisfiedDependency> unsatisfiedDependencies) {
            this.unsatisfiedDependencies = unsatisfiedDependencies;
        }

        public ImmutableList<UnsatisfiedDependency> getUnsatisfiedDependencies() {
            return unsatisfiedDependencies;
        }

        @Override
        public String toString() {
            return Joiner.on("\n").join(unsatisfiedDependencies);
        }

        public boolean isEmpty() {
            return unsatisfiedDependencies.isEmpty();
        }

        public UnsatisfiedDependenciesException raise() {
            return new UnsatisfiedDependenciesException(this);
        }

        public <T> UnsatisfiedDependencies prepend(SatisfiedQuery<T> query) {
            List<UnsatisfiedDependency> deps = new ArrayList<>(unsatisfiedDependencies.size());
            for (UnsatisfiedDependency unsatisfiedDependency : unsatisfiedDependencies) {
                deps.add(unsatisfiedDependency.prepend(query));
            }

            return new UnsatisfiedDependencies(ImmutableList.copyOf(deps));
        }

        public UnsatisfiedDependencies concat(UnsatisfiedDependencies other) {
            return new UnsatisfiedDependencies(ImmutableList.<UnsatisfiedDependency>builder()
                    .addAll(unsatisfiedDependencies).addAll(other.unsatisfiedDependencies).build());
        }
    }

    public static class UnsatisfiedDependenciesException extends IllegalStateException {
        private final UnsatisfiedDependencies unsatisfiedDependencies;

        public UnsatisfiedDependenciesException(UnsatisfiedDependencies unsatisfiedDependencies) {
            super(unsatisfiedDependencies.toString());
            this.unsatisfiedDependencies = unsatisfiedDependencies;
        }

        public UnsatisfiedDependencies getUnsatisfiedDependencies() {
            return unsatisfiedDependencies;
        }
    }

    public static class SatisfiedQuery<T> {
        public static <T> SatisfiedQuery<T> of(Query<T> query, Name<T> name) {
            return new SatisfiedQuery<T>(query, name);
        }

        private final Query<T> query;
        private final Name<T> name;

        private SatisfiedQuery(Query<T> query, Name<T> name) {
            this.query = query;
            this.name = name;
        }

        public Query<T> getQuery() {
            return query;
        }

        public Name<T> getName() {
            return name;
        }

        @Override
        public String toString() {
            return query + " (= " + name + ")";
        }
    }

    private static class BuildingBox<T> {
        final MachineEngine<T> engine;
        final ImmutableList<SatisfiedQuery<?>> hierarchy;
        final Multimap<Query<?>, Name<?>> names = ArrayListMultimap.create();
        final Map<Name<?>, BuildingBox<?>> deps = new LinkedHashMap<>();

        // these 2 fields are used for topological sorting only
        // at the end of the sort they are empty and have non meaning at all
        final Deque<BuildingBox<?>> depsToSort = new LinkedList<>();
        final Deque<BuildingBox<?>> predecessorsToSort = new LinkedList<>();

        SatisfiedBOM satisfiedBOM;
        NamedComponent<T> component;

        BuildingBox(ImmutableList<SatisfiedQuery<?>> hierarchy, MachineEngine<T> engine) {
            this.hierarchy = hierarchy;
            this.engine = engine;
        }

        @Override
        public String toString() {
            return "BuildingBox{" +
                    "name=" + engine.getName() +
                    '}';
        }

        public <D> void addName(Query<D> query, Name<D> name, BuildingBox<D> buildingBox) {
            names.put(query, name);
            deps.put(name, buildingBox);
            depsToSort.add(buildingBox);
            buildingBox.predecessorsToSort.add(this);
        }
    }

    /**
     * Used to load FactoryMachines from ServiceLoader.
     *
     * This class is used to cache FactoryMachines loaded from ServiceLoader, to call it only once per Classloader.
     */
    private static class FactoryMachinesServiceLoader {
        private static Map<ClassLoader, Iterable<? extends FactoryMachine>> serviceLoaderMachines = new WeakHashMap<>();

        static synchronized Iterable<? extends FactoryMachine> getMachines() {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = FactoryMachinesServiceLoader.class.getClassLoader();
            }

            Iterable<? extends FactoryMachine> factoryMachines = serviceLoaderMachines.get(classLoader);
            if (factoryMachines == null) {
                try {
                    factoryMachines = ImmutableList.copyOf(ServiceLoader.load(FactoryMachine.class));
                    serviceLoaderMachines.put(classLoader, factoryMachines);
                } catch (ServiceConfigurationError e) {
                    if (e.getMessage().endsWith("not found")
                            || e.getMessage().indexOf("java.lang.NoClassDefFoundError") != -1) {
                        String resources = "";
                        try {
                            resources =
                                    "\n\n\t\t>> If the problem persists, check these resources:" +
                                            "\n\t\t\t- " + Joiner.on("\n\t\t\t- ").join(
                                            Iterators.forEnumeration(classLoader
                                                    .getResources("META-INF/services/restx.factory.FactoryMachine")));
                        } catch (IOException e1) {
                            // ignore
                        }
                        throw new RuntimeException(e.getMessage() + "." +
                                "\n\t\t>> This may be because you renamed or removed it." +
                                "\n\t\t>> Try to clean and rebuild your application and reload/relaunch." +
                                resources +
                                "\n", e);
                    } else {
                        throw e;
                    }
                }
            }
            return factoryMachines;
        }
    }
}
