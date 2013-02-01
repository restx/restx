package restx.factory;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:43 PM
 */
public final class NamedComponent<T> {
    public static <T> Function<NamedComponent<T>, T> toComponent() {
        return new Function<NamedComponent<T>, T>() {
            @Override
            public T apply(NamedComponent<T> namedComponent) {
                return namedComponent.getComponent();
            }
        };
    }

    public static <T> NamedComponent<T> of(Class<T> clazz, String name, T component) {
        return new NamedComponent<>(new Name<>(clazz, name), component);
    }

    private final Name<T> name;
    private final T component;

    public NamedComponent(Name<T> name, T component) {
        this.name = checkNotNull(name);
        this.component = checkNotNull(component);
    }

    public Name<T> getName() {
        return name;
    }

    public T getComponent() {
        return component;
    }

    @Override
    public String toString() {
        return "NamedComponent{" +
                "name=" + name +
                ", component=" + component +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NamedComponent that = (NamedComponent) o;

        if (!component.equals(that.component)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + component.hashCode();
        return result;
    }
}
