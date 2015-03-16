package restx.factory;

import com.google.common.base.Objects;
import restx.common.TypeReference;

import java.lang.reflect.Type;

/**
 * @author fcamblor
 */
public class ParamDef<T> {
    private final String name;
    private final TypeReference<T> typeRef;
    private final Class<T> primitiveType;

    public ParamDef(TypeReference<T> typeRef, String name) {
        this(name, typeRef, null);
    }

    public ParamDef(Class<T> primitiveType, String name) {
        this(name, null, primitiveType);
    }

    protected ParamDef(String name, TypeReference<T> typeRef, Class<T> primitiveType) {
        this.name = name;
        this.typeRef = typeRef;
        this.primitiveType = primitiveType;
    }

    public static <T> ParamDef<T> of(TypeReference<T> type, String name) {
        return new ParamDef(type, name);
    }

    public static <T> ParamDef<T> of(Class<T> rawType, String name) {
        return new ParamDef(rawType, name);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return isPrimitiveType()?primitiveType:typeRef.getType();
    }

    public boolean isPrimitiveType(){
        return primitiveType != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamDef)) return false;
        ParamDef<?> paramDef = (ParamDef<?>) o;
        return Objects.equal(name, paramDef.name) &&
                Objects.equal(getType(), paramDef.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, getType());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("type", getType())
                .toString();
    }
}
