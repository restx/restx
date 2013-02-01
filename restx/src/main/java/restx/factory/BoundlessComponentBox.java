package restx.factory;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 6:03 PM
 */
public class BoundlessComponentBox<T> implements ComponentBox<T> {
    public static final ComponentBox.BoxFactory FACTORY = new ComponentBox.BoxFactory() {
        public <T> ComponentBox<T> of(NamedComponent<T> namedComponent) {
            return new BoundlessComponentBox<>(namedComponent);
        }
    };

    private final NamedComponent<T> namedComponent;

    public BoundlessComponentBox(NamedComponent<T> namedComponent) {
        this.namedComponent = checkNotNull(namedComponent);
    }

    public synchronized Optional<NamedComponent<T>> pick() {
        return Optional.of(namedComponent);
    }

    @Override
    public Name<T> getName() {
        return namedComponent.getName();
    }
}
