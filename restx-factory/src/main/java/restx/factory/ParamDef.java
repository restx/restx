package restx.factory;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import restx.types.TypeReference;
import restx.types.Types;
import restx.types.AggregateType;

import java.lang.reflect.Type;

/**
 * @author fcamblor
 */
public class ParamDef<T> {
    public interface SpecialParamHandling {
        SpecialParamHandling ARRAYS = new SpecialParamHandling() {
            @Override
            public Object extractValueFrom(ImmutableList<String> values, String fullPath) {
                return values.toArray(new String[0]);
            }
        };
        SpecialParamHandling UNSUPPORTED = new SpecialParamHandling() {
            @Override
            public Object extractValueFrom(ImmutableList<String> values, String fullPath) {
                throw new IllegalArgumentException("Unsupported array of complex type for query parameter "+fullPath);
            }
        };

        Object extractValueFrom(ImmutableList<String> values, String fullPath);
    }

    public static class ParamValuesHandlings {
        public static final ParamValuesHandlings WITHOUT_SPECIAL_PARAMS = new ParamValuesHandlings();

        final ImmutableMap<String, SpecialParamHandling> pathSpecialParamsHandlig;

        protected ParamValuesHandlings() {
            this(ImmutableMap.<String, SpecialParamHandling>of());
        }

        public ParamValuesHandlings(ImmutableMap<String, SpecialParamHandling> pathSpecialParamsHandlig) {
            this.pathSpecialParamsHandlig = pathSpecialParamsHandlig;
        }

        public void fillNode(Map<String, Object> currentNode, String fieldName, ImmutableList<String> values, String fullPath) {
            Object value;
            if(!pathSpecialParamsHandlig.containsKey(fullPath)) {
                value = Iterables.getOnlyElement(values);
            } else {
                value = pathSpecialParamsHandlig.get(fullPath).extractValueFrom(values, fullPath);
            }
            currentNode.put(fieldName, value);
        }
    }

    private final String name;
    private final TypeReference<T> typeRef;
    private final Class<T> primitiveType;
    private final Class rawType;
    private final AggregateType aggregateType;
    private final ParamValuesHandlings paramValuesHandlings;

    public ParamDef(TypeReference<T> typeRef, String name) {
        this(name, typeRef, null, ParamValuesHandlings.WITHOUT_SPECIAL_PARAMS);
    }

    public ParamDef(Class<T> primitiveType, String name) {
        this(name, null, primitiveType, ParamValuesHandlings.WITHOUT_SPECIAL_PARAMS);
    }

    protected ParamDef(ParamDef other) {
        this(other.name, other.typeRef, other.primitiveType, other.paramValuesHandlings);
    }

    protected ParamDef(String name, TypeReference<T> typeRef, Class<T> primitiveType, ParamValuesHandlings paramValuesHandlings) {
        this.name = name;
        this.typeRef = typeRef;
        this.primitiveType = primitiveType;
        this.rawType = Types.getRawType(getType());
        this.aggregateType = Types.aggregateTypeFrom(this.rawType.getCanonicalName()).orNull();
        this.paramValuesHandlings = paramValuesHandlings;
    }

    public static <T> ParamDef<T> of(TypeReference<T> type, String name) {
        return new ParamDef(name, type, null, ParamValuesHandlings.WITHOUT_SPECIAL_PARAMS);
    }

    public static <T> ParamDef<T> of(TypeReference<T> type, String name, ImmutableMap<String, SpecialParamHandling> pathSpecialParamsHandlig) {
        return new ParamDef(name, type, null, new ParamValuesHandlings(pathSpecialParamsHandlig));
    }

    public static <T> ParamDef<T> of(Class<T> rawType, String name) {
        return new ParamDef(name, null, rawType, ParamValuesHandlings.WITHOUT_SPECIAL_PARAMS);
    }

    public static <T> ParamDef<T> of(Class<T> rawType, String name, ImmutableMap<String, SpecialParamHandling> pathSpecialParamsHandlig) {
        return new ParamDef(name, null, rawType, new ParamValuesHandlings(pathSpecialParamsHandlig));
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

    public Class getRawType() {
        return rawType;
    }

    public boolean isAggregateType() {
        return this.aggregateType != null;
    }

    public AggregateType getAggregateType() {
        return this.aggregateType;
    }

    public ParamValuesHandlings getParamValuesHandlings() {
        return paramValuesHandlings;
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
