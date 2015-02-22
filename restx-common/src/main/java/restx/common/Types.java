package restx.common;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.lang.Class;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;

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
                return rawType.getEnclosingClass();
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

	/**
	 * This method acts like the {@link Class#isAssignableFrom(Class)} but with {@link Type}.
	 *
	 * <p>
	 * {@code true} meant that {@code t1} is assignable from {@code t2},
	 * in other words, t1 is a super type of t2.
	 *
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return true if t1 is assignable from t2, false otherwise
	 */
	public static boolean isAssignableFrom(Type t1, Type t2) {
		checkNotNull(t1);
		checkNotNull(t2);

		if (t1.equals(t2)) {
			// easy case, types are equals
			return true;
		}

		if (t1 instanceof Class<?>) {
			/*
				first type is a reifiable class, so any raw type, generic or not,
				which is assignable by the raw type of t1 means that t1 is assignable from t2
				and the reverse is also true
			 */
			return ((Class<?>) t1).isAssignableFrom(getRawType(t2));
		}

		if (t1 instanceof ParameterizedType) {
			Class<?> rawTypeT1 = getRawType(t1);
			Class<?> rawTypeT2 = getRawType(t2);

			if (!rawTypeT1.isAssignableFrom(rawTypeT2) || rawTypeT1.equals(rawTypeT2)) {
				/*
					don't need to go further and to check generics, raw type of first type is not assignable
					from second one, or raw types are equals but type aren't, so has one can not reference
					a same type having a different generic type than his, it also means that t1 is not
					assignable from t2
				 */
				return false;
			}

			if (t2 instanceof Class<?>) {
				/*
					we need to launch a recursive analysis on generic super type, and generic interfaces, because
					t2 is a reifiable class, so there is no way that t1 can be directly assigned from t2,
					so it has to be one of the sub-types of t2
				 */

				Type genericSuperclass = ((Class) t2).getGenericSuperclass();
				if (genericSuperclass != null) {
					if (isAssignableFrom(t1, genericSuperclass)) {
						return true;
					}
				}
				Type[] genericInterfaces = ((Class) t2).getGenericInterfaces();
				for (Type genericInterface : genericInterfaces) {
					if (isAssignableFrom(t1, genericInterface)) {
						return true;
					}
				}

				return false;
			}

			if (t2 instanceof ParameterizedType) {
				/*
					Extract generics type, and variables of the class, in order to know the concrete type of each variables
				 */
				Type[] actualTypeArguments = ((ParameterizedType) t2).getActualTypeArguments();
				TypeVariable<? extends Class<?>>[] typeParameters = rawTypeT2.getTypeParameters();
				Map<TypeVariable<? extends Class<?>>, Type> typesMap = Maps.newHashMapWithExpectedSize(typeParameters.length);
				for (int i = 0; i < typeParameters.length; i++) {
					typesMap.put(typeParameters[i], actualTypeArguments[i]);
				}

				/*
					Try with the superclass, and fill variable with concrete types
				 */
				Type genericSuperclass = rawTypeT2.getGenericSuperclass();
				if (genericSuperclass != null) {
					if (genericSuperclass instanceof Class<?>) {
						if (isAssignableFrom(t1, genericSuperclass)) {
							return true;
						}
					} else if (genericSuperclass instanceof ParameterizedType) {
						/*
							We need to replace type parameters with concrete types
						 */
						ParameterizedType genericSuperclassParameterized = (ParameterizedType) genericSuperclass;
						Type[] genericSuperclassActualTypeArguments = genericSuperclassParameterized.getActualTypeArguments();
						for (int i = 0; i < genericSuperclassActualTypeArguments.length; i++) {
							if (genericSuperclassActualTypeArguments[i] instanceof TypeVariable) {
								genericSuperclassActualTypeArguments[i] = typesMap.get(genericSuperclassActualTypeArguments[i]);
							}
						}
						Type genericSuperclassRawType = genericSuperclassParameterized.getRawType();
						if (isAssignableFrom(t1, Types.newParameterizedType((Class<?>) genericSuperclassRawType,
								genericSuperclassActualTypeArguments))) {
							return true;
						}
					}
				}

				/*
					Try the same thing with interfaces
				 */
				Type[] genericInterfaces = rawTypeT2.getGenericInterfaces();
				for (Type genericInterface : genericInterfaces) {
					if (genericInterface instanceof Class<?>) {
						if (isAssignableFrom(t1, genericInterface)) {
							return true;
						}
					} else if (genericInterface instanceof ParameterizedType) {
						/*
							We need to replace type parameters with concrete types
						 */
						ParameterizedType genericInterfaceParameterized = (ParameterizedType) genericInterface;
						Type[] genericInterfaceActualTypeArguments = genericInterfaceParameterized.getActualTypeArguments();
						for (int i = 0; i < genericInterfaceActualTypeArguments.length; i++) {
							if (genericInterfaceActualTypeArguments[i] instanceof TypeVariable) {
								genericInterfaceActualTypeArguments[i] = typesMap.get(genericInterfaceActualTypeArguments[i]);
							}
						}
						Type genericInterfaceRawType = genericInterfaceParameterized.getRawType();
						if (isAssignableFrom(t1, Types.newParameterizedType((Class<?>) genericInterfaceRawType,
								genericInterfaceActualTypeArguments))) {
							return true;
						}
					}
				}

				return false;

			}
		}
		return false;
	}
}
