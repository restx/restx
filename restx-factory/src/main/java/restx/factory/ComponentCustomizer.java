package restx.factory;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 12:51 AM
 */
public interface ComponentCustomizer<T> {
    int priority();
    NamedComponent<T> customize(NamedComponent<T> namedComponent);
}
