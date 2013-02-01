package restx.factory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 5:40 PM
 */
public final class Name<T> {
    private final String name;
    private final Class<T> clazz;

    public static <T> Name<T> of(Class<T> clazz, String name) {
        return new Name<>(clazz, name);
    }

    public Name(Class<T> clazz, String name) {
        this.name = checkNotNull(name);
        this.clazz = checkNotNull(clazz);
    }

    public String getName() {
        return name;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    @Override
    public String toString() {
        return "Name{" +
                "name='" + name + '\'' +
                ", clazz=" + clazz +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Name name1 = (Name) o;

        if (!clazz.equals(name1.clazz)) return false;
        if (!name.equals(name1.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + clazz.hashCode();
        return result;
    }
}
