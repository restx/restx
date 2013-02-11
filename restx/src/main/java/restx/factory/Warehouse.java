package restx.factory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.jamonapi.Monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:47 PM
 */
public class Warehouse {
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

    private final ConcurrentMap<Name<?>, StoredBox<?>> boxes = new ConcurrentHashMap<>();

    <T> Optional<NamedComponent<T>> checkOut(Name<T> name) {
        StoredBox<T> storedBox = (StoredBox<T>) boxes.get(name);
        if (storedBox != null) {
            return storedBox.box.pick();
        }
        return Optional.absent();
    }

    <T> void checkIn(ComponentBox<T> componentBox, SatisfiedBOM satisfiedBOM, Monitor stop) {
        boxes.put(componentBox.getName(), new StoredBox(componentBox, satisfiedBOM, stop.getMax()));
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
