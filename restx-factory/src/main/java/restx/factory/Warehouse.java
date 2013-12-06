package restx.factory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:47 PM
 */
public class Warehouse implements AutoCloseable {
    private static final AtomicLong ID = new AtomicLong();

    public Warehouse() {
        this(ImmutableList.<Warehouse>of());
    }

    public Warehouse(ImmutableList<Warehouse> providers) {
        this.providers = providers;
        StringBuilder sb = new StringBuilder();
        for (Warehouse provider : providers) {
            sb.append("<<").append(provider.getId());
        }
        this.id = String.format("%03d%s", ID.incrementAndGet(), sb.toString());
    }

    public ImmutableList<Warehouse> getProviders() {
        return providers;
    }

    public static class StoredBox<T> {
        private final ComponentBox<T> box;
        private final SatisfiedBOM satisfiedBOM;
        private final long maxTime;

        private StoredBox(ComponentBox<T> box, SatisfiedBOM satisfiedBOM, long maxTime) {
            this.box = box;
            this.satisfiedBOM = satisfiedBOM;
            this.maxTime = maxTime;
        }

        public ComponentBox<T> getBox() {
            return box;
        }

        public SatisfiedBOM getSatisfiedBOM() {
            return satisfiedBOM;
        }

        public long getMaxTime() {
            return maxTime;
        }

        @Override
        public String toString() {
            return "StoredBox{" +
                    "box=" + box +
                    ", satisfiedBOM=" + satisfiedBOM +
                    ", maxTime=" + maxTime +
                    '}';
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Warehouse.class);

    private final String id;
    private final ConcurrentMap<Name<?>, StoredBox<?>> boxes = new ConcurrentHashMap<>();
    private final ImmutableList<Warehouse> providers;

    public String getId() {
        return id;
    }

    <T> Optional<StoredBox<T>> getStoredBox(Name<T> name) {
        return Optional.fromNullable((StoredBox<T>) boxes.get(name));
    }

    <T> Optional<NamedComponent<T>> checkOut(Name<T> name) {
        StoredBox<T> storedBox = (StoredBox<T>) boxes.get(name);
        if (storedBox != null) {
            return storedBox.box.pick();
        }

        for (Warehouse provider : providers) {
            Optional<NamedComponent<T>> component = provider.checkOut(name);
            if (component.isPresent()) {
                return component;
            }
        }

        return Optional.absent();
    }

    <T> void checkIn(ComponentBox<T> componentBox, SatisfiedBOM satisfiedBOM, long maxTime) {
        StoredBox<?> previousBox = boxes.put(componentBox.getName(), new StoredBox(componentBox, satisfiedBOM, maxTime));
        if (previousBox != null) {
            try {
                previousBox.box.close();
            } catch (Exception e) {
                logger.warn("exception raised when closing box " + previousBox.box, e);
            }
        }
    }

    public void close() {
        Collection<Exception> exceptions = Lists.newArrayList();
        for (StoredBox<?> storedBox : boxes.values()) {
            try {
                storedBox.box.close();
            } catch (Exception e) {
                logger.warn("exception while closing " + storedBox.box, e);
                exceptions.add(e);
            }
        }
        boxes.clear();
        if (!exceptions.isEmpty()) {
            if (exceptions.size() == 1) {
                throw new IllegalStateException("exception raised while closing warehouse",
                        exceptions.iterator().next());
            }
            throw new IllegalStateException("exceptions raised when closing warehouse."
                    + " Exceptions: " + Joiner.on(", ").join(exceptions));
        }
    }


    public Iterable<Name<?>> listNames() {
        Set<Name<?>> names = new LinkedHashSet<>();
        names.addAll(boxes.keySet());
        for (Warehouse provider : providers) {
            Iterables.addAll(names, provider.listNames());
        }

        return ImmutableSet.copyOf(names);
    }

    public Iterable<Name<?>> listDependencies(Name name) {
        StoredBox storedBox = boxes.get(name);
        if (storedBox != null) {
            Collection<Name<?>> deps = Lists.newArrayList();
            for (NamedComponent<? extends Object> namedComponent : storedBox.satisfiedBOM.getAllComponents()) {
                deps.add(namedComponent.getName());
            }
            return deps;
        } else {
            for (Warehouse provider : providers) {
                Iterable<Name<?>> deps = provider.listDependencies(name);
                if (!Iterables.isEmpty(deps)) {
                    return deps;
                }
            }

            return Collections.emptySet();
        }
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "boxes=" + boxes +
                "; providers=" + providers +
                '}';
    }
}
