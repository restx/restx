package restx.common;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * User: xavierhanin
 * Date: 2/3/13
 * Time: 1:39 PM
 */
public class MorePreconditions {
    public static <T> T checkPresent(Optional<T> optional, String msg, Object... parameters) {
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new IllegalStateException(String.format(msg, parameters));
        }
    }

    public static <T> T checkEquals(String name1, T o1, String name2, T o2) {
        Preconditions.checkArgument(Objects.equal(o1, o2),
                "%s %s must be equal to %s %s", name1, o1, name2, o2);

        return o1;
    }

    public static <T> T checkInstanceOf(String name, Object o, Class<T> clazz) {
        Preconditions.checkArgument(clazz.isInstance(o),
                "%s %s must be an instanceof of %s", name, o, clazz.getSimpleName());

        return (T) o;
    }

    public static <K,V> V checkContainsKey(String name, Map<K,V> map, K key) {
        Preconditions.checkArgument(map.containsKey(key),
                "%s map must contain key value for key %s", name, key);
        return map.get(key);
    }
}
