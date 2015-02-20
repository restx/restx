package restx.common;

import static java.lang.String.format;


import java.lang.Class;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

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

	/**
	 * Gets the raw type of the specified type.
	 *
	 * <p>
	 * As defined by <a href="http://docs.oracle.com/javase/tutorial/java/generics/rawTypes.html">
	 *     http://docs.oracle.com/javase/tutorial/java/generics/rawTypes.html</a>
	 * The raw type is the name of a generic class or interface without any type arguments.
	 * <p>
	 * This method know only how to extract raw type of {@link Class}, {@link ParameterizedType} and {@link GenericArrayType}.
	 * Calling this method with an instance of {@link TypeVariable} or {@link WildcardType} will throw an
	 * {@link IllegalArgumentException}.
	 *
	 * @param type the type
	 * @return the raw type corresponding to the specified type
	 * @throws IllegalArgumentException if the method is called with a type not being an instance of {@link Class},
	 * {@link ParameterizedType} or {@link GenericArrayType}.
	 */
	public static Class<?> getRawType(Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		}

		if (type instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) type).getRawType();
			if (rawType instanceof Class<?>) {
				return (Class<?>) rawType;
			}
			throw new IllegalStateException(
					format("getRawType of the parameterized type %s did not return a class, but %s", type, rawType));
		}

		if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			return Array.newInstance(getRawType(componentType), 0).getClass();
		}

		throw new IllegalArgumentException(
				format("Unhandled type %s, unable to extract its raw type.", type));
	}
}
