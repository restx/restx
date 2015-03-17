package restx.factory;

import java.util.Objects;
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

    protected ParamDef(ParamDef other) {
        this(other.name, other.typeRef, other.primitiveType);
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
        return Objects.equals(name, paramDef.name) &&
                Objects.equals(getType(), paramDef.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getType());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParamDef{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type=").append(getType());
        sb.append('}');
        return sb.toString();
    }
}
