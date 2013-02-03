package restx.factory;

import com.google.common.base.Optional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:47 PM
 */
public class Warehouse {
    private final ConcurrentMap<Name<?>, ComponentBox<?>> boxes = new ConcurrentHashMap<>();

    public <T> Optional<NamedComponent<T>> checkOut(Name<T> name) {
        ComponentBox<T> box = (ComponentBox<T>) boxes.get(name);
        if (box != null) {
            return box.pick();
        }
        return Optional.absent();
    }

    public <T> void checkIn(ComponentBox<T> componentBox) {
        boxes.put(componentBox.getName(), componentBox);
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "boxes=" + boxes +
                '}';
    }
}
