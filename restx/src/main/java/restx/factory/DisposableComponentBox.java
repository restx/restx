package restx.factory;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 6:03 PM
 */
public class DisposableComponentBox<T> implements ComponentBox<T> {
    public static final ComponentBox.BoxFactory FACTORY = new ComponentBox.BoxFactory() {
        public <T> ComponentBox<T> of(NamedComponent<T> namedComponent) {
            return new DisposableComponentBox<>(namedComponent);
        }

        @Override
        public String toString() {
            return "DisposableComponentBox.FACTORY";
        }
    };

    private final Name<T> name;
    private NamedComponent<T> namedComponent;
    private boolean disposed;

    public DisposableComponentBox(NamedComponent<T> namedComponent) {
        name = namedComponent.getName();
        this.namedComponent = namedComponent;
    }

    @Override
    public synchronized ComponentBox<T> customize(ComponentCustomizer<T> customizer) {
        if (!disposed && namedComponent != null) {
            namedComponent = customizer.customize(namedComponent);
        }
        return this;
    }

    @Override
    public synchronized void close() {
        if (namedComponent.getComponent() instanceof AutoCloseable) {
            try {
                ((AutoCloseable) namedComponent.getComponent()).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        namedComponent = null;
        disposed = true;
    }

    public synchronized Optional<NamedComponent<T>> pick() {
        if (disposed) {
            return Optional.absent();
        }
        Optional<NamedComponent<T>> picked = Optional.fromNullable(namedComponent);
        // we don't clear the reference to the component, we may need it to clean it when closing the box
        disposed = true;
        return picked;
    }

    @Override
    public String toString() {
        return "DisposableComponentBox{" +
                "name=" + name +
                ", namedComponent=" + namedComponent +
                '}';
    }

    @Override
    public Name<T> getName() {
        return name;
    }
}
