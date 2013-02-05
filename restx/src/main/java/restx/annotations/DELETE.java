package restx.annotations;

import com.google.common.collect.ImmutableMap;

/**
 * User: xavierhanin
 * Date: 1/18/13
 * Time: 9:50 PM
 */
public @interface DELETE {
    String value();

    public static final ImmutableMap DELETED = ImmutableMap.of("status", "deleted");
}
