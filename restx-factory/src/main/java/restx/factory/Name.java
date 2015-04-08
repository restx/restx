package restx.factory;

import static com.google.common.base.Preconditions.checkNotNull;


import java.lang.reflect.Type;
import restx.common.MoreObjects;
import restx.common.TypeReference;
import restx.common.Types;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:40 PM
 */
public final class Name<T> {
    private final String name;
	private final Type type;
	private final Class<T> rawType;

    public static <T> Name<T> of(Class<T> clazz, String name) {
        return new Name<>(clazz, clazz, name);
    }

    public static <T> Name<T> of(Class<T> clazz) {
        return new Name<>(clazz, clazz, clazz.getSimpleName());
    }

	public static <T> Name<T> of(Type type, String name) {
		return new Name<>(type, name);
	}

	@SuppressWarnings("unchecked")
	public static <T> Name<T> of(Type type) {
		Class<T> rawType = (Class<T>) Types.getRawType(type);
		return new Name<>(type, rawType, rawType.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	public static <T> Name<T> of(String name, Class<?> rawType, Type... arguments) {
		return (Name<T>) new Name<>(Types.newParameterizedType(rawType, arguments), rawType, name);
	}

	public static <T> Name<T> of(TypeReference<T> typeReference, String name) {
		return new Name<>(checkNotNull(typeReference).getType(), name);
	}

	@SuppressWarnings("unchecked")
	public Name(Type type, String name) {
		this(type,  (Class<T>) Types.getRawType(type), name);
	}

    private Name(Type type, Class<T> rawType, String name) {
        this.name = checkNotNull(name);
		this.type = checkNotNull(type);
		this.rawType = checkNotNull(rawType);
    }

    public String getSimpleName() {
        String simpleName = String.valueOf(type);
        if (!simpleName.equalsIgnoreCase(name)) {
            simpleName = name + "[" + simpleName + "]";
        }
        return simpleName;
    }

    public String asId() {
        return "[" + String.valueOf(type) + "]" + name;
    }

    public String getName() {
        return name;
    }

	public Type getType() {
		return type;
	}

    public Class<T> getClazz() {
		return rawType;
	}

    @Override
    public String toString() {
        return "Name{" +
                "name='" + name + '\'' +
                ", type=" + MoreObjects.toString(type) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Name name1 = (Name) o;

        if (!rawType.equals(name1.rawType)) return false;
        if (!type.equals(name1.type)) return false;
        if (!name.equals(name1.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + rawType.hashCode();
		result = 31 * result + type.hashCode();
        return result;
    }
}
