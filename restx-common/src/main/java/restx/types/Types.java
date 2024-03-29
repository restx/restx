package restx.types;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import restx.types.optional.OptionalTypeDefinition;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Date: 23/10/13
 * Time: 11:02
 */
public class Types {

	public static final ImmutableList<AggregateType> DECLARED_AGGREGATED_TYPES;
	public static final ImmutableList<OptionalTypeDefinition> DECLARED_OPTIONAL_TYPES;
	public static final ImmutableList<RawTypesDefinition> DECLARED_RAW_TYPES_DEFINITIONS;
	static {
		DECLARED_AGGREGATED_TYPES = ImmutableList.copyOf(ServiceLoader.load(AggregateType.class));
		DECLARED_OPTIONAL_TYPES = ImmutableList.copyOf(ServiceLoader.load(OptionalTypeDefinition.class));
		DECLARED_RAW_TYPES_DEFINITIONS = ImmutableList.copyOf(ServiceLoader.load(RawTypesDefinition.class));
	}

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

	public static boolean matchesParameterizedFQCN(Class c, String fqcn) {
		return fqcn.startsWith(c.getCanonicalName());
	}

	public static Class aggregatedTypeOf(Type type) {
		if(type instanceof ParameterizedType) {
			return (Class)((ParameterizedType)type).getActualTypeArguments()[0];
		} else if(type instanceof Class && ((Class)type).isArray()){
			return ((Class)type).getComponentType();
		} else {
			throw new IllegalArgumentException("Call to aggregatedTypeOf() is not supported for type : "+type);
		}
	}

	public static TypeMirror aggregatedTypeOf(TypeMirror type) {
		if (type instanceof ArrayType) {
			ArrayType arrayType = (ArrayType) type;
			return arrayType.getComponentType();
		} else if (type instanceof DeclaredType) {
			DeclaredType declaredType = (DeclaredType) type;
			return Iterables.getOnlyElement(declaredType.getTypeArguments());
		} else {
			throw new IllegalArgumentException("Call to aggregatedTypeOf() is not supported for type : " + type);
		}
	}

	public static Optional<AggregateType> aggregateTypeFrom(String fqcn) {
		for(AggregateType aggregateType : DECLARED_AGGREGATED_TYPES){
			if(aggregateType.isApplicableTo(fqcn)) {
				return Optional.of(aggregateType);
			}
		}

		return Optional.absent();
	}
	public static boolean isAggregateType(String fqcn) {
		return aggregateTypeFrom(fqcn).isPresent();
	}

	public static OptionalTypeDefinition.Matcher optionalMatchingTypeOf(TypeMirror type, final List<String> additionalAnnotationClasses) {
		OptionalTypeDefinition.Matcher lastMatcher = null;
		for (OptionalTypeDefinition optionalDefinition: DECLARED_OPTIONAL_TYPES) {
			lastMatcher = optionalDefinition.matches(type, additionalAnnotationClasses);
			if (lastMatcher.isOptionalType()) {
				return lastMatcher;
			}
		}

		return lastMatcher;
	}

	public static boolean isEnum(TypeMirror typeMirror, javax.lang.model.util.Types typeUtils) {
		TypeMirror firstAncestor = Iterables.getFirst(typeUtils.directSupertypes(typeMirror), null);
		if(firstAncestor == null) {
			return false;
		}
		return firstAncestor.toString().startsWith("java.lang.Enum<");
	}

	public static boolean isRawType(Type type) {
		for (RawTypesDefinition rawTypesDefinition : DECLARED_RAW_TYPES_DEFINITIONS) {
			if (rawTypesDefinition.accepts(type)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRawType(TypeMirror type, ProcessingEnvironment processingEnvironment) {
		for (RawTypesDefinition rawTypesDefinition : DECLARED_RAW_TYPES_DEFINITIONS) {
			if (rawTypesDefinition.accepts(type, processingEnvironment)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isParameterizedType(String type) {
		return type.contains("<");
	}

	public static String rawTypeFrom(String type) {
		return isParameterizedType(type)?type.substring(0, type.indexOf("<")):type;
	}

	public static TypeMirror primitiveTypeMirrorToBoxed(TypeMirror typeMirror, ProcessingEnvironment processingEnvironment) {
		if (typeMirror.getKind().isPrimitive()) {
			return processingEnvironment.getTypeUtils().boxedClass((PrimitiveType) typeMirror).asType();
		} else {
			return typeMirror;
		}
	}
}
