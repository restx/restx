package restx.common;

/**
 * User: xavierhanin
 * Date: 1/28/13
 * Time: 6:00 PM
 */
public interface Visitor<T> {
    void visit(T item);
}
