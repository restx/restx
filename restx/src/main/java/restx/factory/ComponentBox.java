package restx.factory;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 6:03 PM
 */
public interface ComponentBox<T> extends AutoCloseable {
    public static interface BoxFactory {
        <T> ComponentBox<T> of(NamedComponent<T> namedComponent);
    }

    Optional<NamedComponent<T>> pick();
    Name<T> getName();
}
