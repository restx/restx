package restx.factory;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:54 PM
 */
public class ComponentBoxWrapper<T> implements ComponentBox<T> {
    private final ComponentBox<T> original;

    public ComponentBoxWrapper(ComponentBox<T> original) {
        this.original = original;
    }

    public Optional<NamedComponent<T>> pick() {
        return original.pick();
    }

    public void close() throws Exception {
        original.close();
    }

    public Name<T> getName() {
        return original.getName();
    }
}
