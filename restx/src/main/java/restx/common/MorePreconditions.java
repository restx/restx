package restx.common;

import com.google.common.base.Optional;

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
}
