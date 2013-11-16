package restx.factory;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MoreStrings.indent;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:42 PM
 */
public class Factory implements AutoCloseable {
    private static final String SERVICE_LOADER = "ServiceLoader";
    private final Logger logger = LoggerFactory.getLogger(Factory.class);
    private static final Name<Factory> FACTORY_NAME = Name.of(Factory.class, "FACTORY");
    private static final Name<MetricRegistry> METRICS_NAME = Name.of(MetricRegistry.class, "METRICS");
    private static final Comparator<ComponentCustomizer> customizerComparator = new Comparator<ComponentCustomizer>() {
        @Override
        public int compare(ComponentCustomizer o1, ComponentCustomizer o2) {
            return Ordering.natural().compare(o1.priority(), o2.priority());
        }
    };
    private static final AtomicLong ID = new AtomicLong();

    public static class LocalMachines {
        private static final ThreadLocal<LocalMachines> threadLocals = new ThreadLocal() {
            @Override
            protected LocalMachines initialValue() {
                return new LocalMachines("");
            }
        };

        private static final ConcurrentMap<String, LocalMachines> contextLocals = new ConcurrentHashMap<>();
        private static final AtomicLong IDS = new AtomicLong();
        private final String id;

        public LocalMachines(String ctxName) {
            id = String.format("CTX[%s][$03d]", ctxName, IDS.incrementAndGet());
        }

        public static LocalMachines threadLocal() {
            return threadLocals.get();
        }

        public static LocalMachines contextLocal(String ctxName) {
            contextLocals.putIfAbsent(ctxName, new LocalMachines(ctxName));
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
    }

    public static class Builder {
        private boolean usedServiceLoader;
        private Multimap<String, FactoryMachine> machines = ArrayListMultimap.create();
        private List<Warehouse> providers = new ArrayList<>();
        private MetricRegistry metrics;

        public Builder addFromServiceLoader() {
            try {
                usedServiceLoader = true; // we have to store separately, in case the list is empty multimap
                // doesn't keep the key
                machines.putAll(SERVICE_LOADER, ServiceLoader.load(FactoryMachine.class));
                return this;
            } catch (ServiceConfigurationError e) {
                if (e.getMessage().endsWith("not found")) {
                    String resources = "";
                    try {
                        resources =
                                "\n\n\t\t>> If the problem persists, check these resources:" +
                                "\n\t\t\t- " + Joiner.on("\n\t\t\t- ").join(
                                Iterators.forEnumeration(Thread.currentThread().getContextClassLoader()
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
            this.metrics = metrics;
            return this;
        }

        public Factory build() {
            if (metrics == null) {
                metrics = new MetricRegistry();
            }

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
                    new Warehouse(ImmutableList.copyOf(providers)), metrics);

            Map<Name<FactoryMachine>, MachineEngine<FactoryMachine>> toBuild = new LinkedHashMap<>();
            ImmutableList<FactoryMachine> factoryMachines = buildFactoryMachines(factory, factory.machines, toBuild);
            while (!factoryMachines.isEmpty()) {
                machines.putAll("FactoryMachines", factoryMachines);
                factory = new Factory(usedServiceLoader, machines,
                        ImmutableList.<ComponentCustomizerEngine>of(), new Warehouse(), metrics);
                factoryMachines = buildFactoryMachines(factory, factoryMachines, toBuild);
            }

            factory = new Factory(usedServiceLoader, machines,
                    buildCustomizerEngines(factory), new Warehouse(ImmutableList.copyOf(providers)), metrics);
            return factory;
        }

        private ImmutableList<ComponentCustomizerEngine> buildCustomizerEngines(Factory factory) {
            List<ComponentCustomizerEngine> componentCustomizerEngines = new ArrayList<>();
            for (FactoryMachine machine : factory.machines) {
                Set<Name<ComponentCustomizerEngine>> names = machine.nameBuildableComponents(ComponentCustomizerEngine.class);
                for (Name<ComponentCustomizerEngine> name : names) {
                    Optional<NamedComponent<ComponentCustomizerEngine>> customizer =
                            factory.buildAndStore(name, machine.getEngine(name));
                    componentCustomizerEngines.add(customizer.get().getComponent());
                }
            }
            return ImmutableList.copyOf(componentCustomizerEngines);
        }

        private ImmutableList<FactoryMachine> buildFactoryMachines(
                Factory factory, ImmutableList<FactoryMachine> factoryMachines,
                Map<Name<FactoryMachine>, MachineEngine<FactoryMachine>> toBuild) {
            List<FactoryMachine> machines = new ArrayList<>();
            StringBuilder notSatisfied = new StringBuilder();
            Map<Name<FactoryMachine>, MachineEngine<FactoryMachine>> moreToBuild = new LinkedHashMap<>();
            for (FactoryMachine machine : factoryMachines) {
                Set<Name<FactoryMachine>> names = machine.nameBuildableComponents(FactoryMachine.class);
                for (Name<FactoryMachine> name : names) {
                    MachineEngine<FactoryMachine> engine = machine.getEngine(name);
                    try {
                        machines.add(factory.buildAndStore(name, engine).get().getComponent());
                    } catch (IllegalStateException e) {
                        moreToBuild.put(name, engine);
                        notSatisfied.append(name).append("\n").append(indent("-> " + e.getMessage(), 2)).append("\n");
                    }
                }
            }

            for (Map.Entry<Name<FactoryMachine>, MachineEngine<FactoryMachine>> entry : new ArrayList<>(toBuild.entrySet())) {
                try {
                    machines.add(factory.buildAndStore(entry.getKey(), entry.getValue()).get().getComponent());
                    toBuild.remove(entry.getKey());
                } catch (IllegalStateException e) {
                    notSatisfied.append(entry.getKey()).append("\n").append(indent("-> " + e.getMessage(), 2)).append("\n");
                }
            }

            toBuild.putAll(moreToBuild);

            if (notSatisfied.length() > 0 // some deps were not satisfied
                    && machines.isEmpty() // and we produced no new machines, so there is no chance we satisfy them later
                    ) {
                throw new IllegalStateException(notSatisfied.toString());
            }

            return ImmutableList.copyOf(machines);
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
        public final Optional<NamedComponent<T>> findOne() {
            checkSatisfy();
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
            checkSatisfy();
            return doFind();
        }
        public final Set<T> findAsComponents() {
            return Sets.newLinkedHashSet(
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
                throw new IllegalStateException(String.format("component satisfying %s not found.%s", this));
            }
            Factory f = factory();
            for (Name<T> name : names) {
                f.checkSatisfy(name);
            }
        }
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
        public Set<Name<Factory>> findNames() {
            return Collections.singleton(FACTORY_NAME);
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
            return new NameQuery(mayGetFactory(), mandatory, getName());
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
        public Set<Name<T>> findNames() {
            return Collections.singleton(name);
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
            return new ClassQuery(mayGetFactory(), mandatory, getComponentClass());
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
        public Set<Name<T>> findNames() {
            return factory().collectAllBuildableNames(componentClass);
        }

        @Override
        public String toString() {
            return "QueryByClass{" +
                    "componentClass=" + componentClass +
                    '}';
        }
    }

    private static final class CanBuildPredicate implements Predicate<FactoryMachine> {
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
    private final MetricRegistry metrics;

    private Factory(boolean usedServiceLoader, Multimap<String, FactoryMachine> machines,
                    ImmutableList<ComponentCustomizerEngine> customizerEngines, Warehouse warehouse,
                    MetricRegistry metrics) {
        this.usedServiceLoader = usedServiceLoader;
        this.customizerEngines = customizerEngines;
        this.metrics = metrics;

        ImmutableMultimap.Builder<String, FactoryMachine> machineBuilder = ImmutableMultimap.<String, FactoryMachine>builder()
                .put("FactoryMachine", new SingletonFactoryMachine<>(10000, new NamedComponent<>(FACTORY_NAME, this)))
                .put("MetricRegistryMachine", new SingletonFactoryMachine<>(10000, new NamedComponent<>(METRICS_NAME, metrics)));
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
        this.id = String.format("%03d(%d)", ID.incrementAndGet(), machinesByBuilder.size());
        this.warehouse = checkNotNull(warehouse);
    }

    public Factory concat(FactoryMachine machine) {
        Multimap<String, FactoryMachine> machines = ArrayListMultimap.create();
        machines.putAll(machinesByBuilder);
        machines.removeAll("FactoryMachine");
        machines.removeAll("MetricRegistryMachine");
        machines.removeAll("WarehouseProvidersMachine");
        machines.put("IndividualMachines", machine);
        return new Factory(usedServiceLoader, machines, customizerEngines,
                new Warehouse(warehouse.getProviders()), metrics);
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
        return new NameQuery(name).bind(this);
    }

    public <T> Query<T> queryByClass(Class<T> componentClass) {
        return new ClassQuery(componentClass).bind(this);
    }

    /**
     * Gets a component by class.
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
        return queryByClass(componentClass).mandatory().findOneAsComponent().get();
    }

    /**
     * Gets a component by name.
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
        return queryByName(componentName).mandatory().findOneAsComponent().get();
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

    private void dumpBuidableComponents(StringBuilder sb) {
        sb.append("--> Buildable Components\n");
        Iterable<Name<Object>> buildableNames = collectAllBuildableNames(Object.class);
        for (Name<?> buildableName : buildableNames) {
            sb.append("   ").append(buildableName).append("\n");
            List<FactoryMachine> allMachinesFor = findAllMachinesFor(buildableName);
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
                        } catch (IllegalStateException ex) {
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
            if (machine.nameBuildableComponents(Object.class).contains(name)) {
                machinesFor.add(machine);
            }
        }
        return machinesFor;
    }

    private List<FactoryMachine> findAllMachinesFor(Name<?> name) {
        return Lists.newArrayList(Iterables.filter(machines, new CanBuildPredicate(name)));
    }

    private Optional<FactoryMachine> findMachineFor(Name<?> name) {
        return Optional.fromNullable(Iterables.find(machines, new CanBuildPredicate(name), null));
    }

    private <T> Set<Name<T>> collectAllBuildableNames(Class<T> componentClass) {
        Set<Name<T>> buildableNames = Sets.newLinkedHashSet();
        for (FactoryMachine machine : machines) {
            buildableNames.addAll(machine.nameBuildableComponents(componentClass));
        }
        return buildableNames;
    }

    private <T> Optional<NamedComponent<T>> buildAndStore(Name<T> name, FactoryMachine machine) {
        if (!machine.canBuild(name)) {
            return Optional.absent();
        }

        MachineEngine<T> engine = machine.getEngine(name);
        return buildAndStore(name, engine);
    }

    private <T> Optional<NamedComponent<T>> buildAndStore(Name<T> name, MachineEngine<T> engine) {
        BillOfMaterials bom = engine.getBillOfMaterial();
        SatisfiedBOM satisfiedBOM = satisfy(name, bom);

        logger.info("building {} with {}", name, engine);
        Timer timer = metrics.timer("<BUILD> " + name.getSimpleName());
        Timer.Context context = timer.time();
        ComponentBox<T> box;
        try {
            box = engine.newComponent(satisfiedBOM);
        } finally {
            context.stop();
        }

        if (box instanceof BoundlessComponentBox && box.pick().isPresent()
                && box.pick().get().getComponent() == this) {
            // do not store nor customize the factory itself
            // to prevent stack overflow on close
            return box.pick();
        }

        List<ComponentCustomizer> customizers = Lists.newArrayList();
        for (ComponentCustomizerEngine customizerEngine : customizerEngines()) {
            if (customizerEngine.canCustomize(box.getName())) {
                customizers.add(customizerEngine.getCustomizer(box.getName()));
            }
        }
        for (ComponentCustomizer customizer : Ordering.from(customizerComparator).sortedCopy(customizers)) {
            context = metrics.timer("<CUSTOMIZE> " + name.getSimpleName()
                    + " <WITH> " + customizer.getClass().getSimpleName()).time();
            try {
                logger.info("customizing {} with {}", name, customizer);
                box = box.customize(customizer);
            } finally {
                context.stop();
            }
        }

        warehouse.checkIn(box, satisfiedBOM, timer.getSnapshot().getMax());
        return warehouse.checkOut(box.getName());
    }

    private Iterable<ComponentCustomizerEngine> customizerEngines() {
        return customizerEngines;
    }

    private SatisfiedBOM satisfy(Name name, BillOfMaterials bom) {
        logger.info("satisfying BOM for {} - {}", name, bom);
        ImmutableMultimap.Builder<Query<?>, NamedComponent<?>> materials = ImmutableMultimap.builder();

        for (Query key : bom.getQueries()) {
            materials.putAll(key, key.bind(this).find());
        }

        return new SatisfiedBOM(bom, materials.build());
    }

    private <T> void checkSatisfy(Name<T> name) {
        Optional<FactoryMachine> machineFor = findMachineFor(name);
        if (!machineFor.isPresent()) {
            Set<Name<T>> similarNames = queryByClass(name.getClazz()).findNames();
            throw new IllegalStateException(name + " can't be satisfied: no machine found to build it." +
                    (similarNames.isEmpty() ? ""
                            : " similar components found: " + Joiner.on(", ").join(similarNames)));
        }

        BillOfMaterials billOfMaterial = machineFor.get().getEngine(name).getBillOfMaterial();
        StringBuilder notSatisfied = new StringBuilder();
        for (Query<?> query : billOfMaterial.getQueries()) {
            try {
                query.bind(this).checkSatisfy();
            } catch (IllegalStateException e) {
                notSatisfied.append(name).append("\n")
                        .append(indent("-> " + e.getMessage(), 2)).append("\n");
            }
        }
        if (notSatisfied.length() > 0) {
            throw new IllegalStateException(notSatisfied.toString());
        }
    }


}
