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

    public DisposableComponentBox(NamedComponent<T> namedComponent) {
        name = namedComponent.getName();
        this.namedComponent = namedComponent;
    }

    public synchronized Optional<NamedComponent<T>> pick() {
        Optional<NamedComponent<T>> picked = Optional.fromNullable(namedComponent);
        namedComponent = null;
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
