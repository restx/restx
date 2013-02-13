package restx.factory;

import com.google.common.base.Optional;

import java.io.Closeable;
import java.io.IOException;

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

        @Override
        public String toString() {
            return "BoundlessComponentBox.FACTORY";
        }
    };

    private final NamedComponent<T> namedComponent;

    public BoundlessComponentBox(NamedComponent<T> namedComponent) {
        this.namedComponent = checkNotNull(namedComponent);
    }

    @Override
    public void close() {
        if (namedComponent.getComponent() instanceof AutoCloseable) {
            try {
                ((AutoCloseable) namedComponent.getComponent()).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized Optional<NamedComponent<T>> pick() {
        return Optional.of(namedComponent);
    }

    @Override
    public Name<T> getName() {
        return namedComponent.getName();
    }

    @Override
    public String toString() {
        return "BoundlessComponentBox{" +
                "namedComponent=" + namedComponent +
                '}';
    }
}
