package restx.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Date: 23/10/13
 * Time: 11:02
 */
public class Types {
    public static ParameterizedType newParameterizedType(final Class<?> rawType, final Type... arguments) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return arguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
