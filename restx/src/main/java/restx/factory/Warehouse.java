package restx.factory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.jamonapi.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:47 PM
 */
public class Warehouse implements AutoCloseable {
    private static class StoredBox<T> {
        private final ComponentBox<T> box;
        private final SatisfiedBOM satisfiedBOM;
        private final double maxTime;

        private StoredBox(ComponentBox<T> box, SatisfiedBOM satisfiedBOM, double maxTime) {
            this.box = box;
            this.satisfiedBOM = satisfiedBOM;
            this.maxTime = maxTime;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(Warehouse.class);

    private final ConcurrentMap<Name<?>, StoredBox<?>> boxes = new ConcurrentHashMap<>();

    <T> Optional<NamedComponent<T>> checkOut(Name<T> name) {
        StoredBox<T> storedBox = (StoredBox<T>) boxes.get(name);
        if (storedBox != null) {
            return storedBox.box.pick();
        }
        return Optional.absent();
    }

    <T> void checkIn(ComponentBox<T> componentBox, SatisfiedBOM satisfiedBOM, Monitor stop) {
        StoredBox<?> previousBox = boxes.put(componentBox.getName(), new StoredBox(componentBox, satisfiedBOM, stop.getMax()));
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
        return ImmutableSet.copyOf(boxes.keySet());
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
            return Collections.emptySet();
        }
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "boxes=" + boxes +
                '}';
    }
}
