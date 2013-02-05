package restx.common;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

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
}
