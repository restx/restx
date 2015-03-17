package restx.endpoint;

import com.google.common.base.Objects;
import restx.common.TypeReference;
import restx.factory.ParamDef;

/**
 * Created by fcamblor on 07/02/15.
 */
public class EndpointParamDef<T> extends ParamDef<T> {
    private final Endpoint endpoint;

    public EndpointParamDef(String method, String pathPattern, TypeReference<T> parameterType, String parameterName) {
        this(Endpoint.of(method, pathPattern), parameterType, parameterName);
    }

    public EndpointParamDef(Endpoint endpoint, ParamDef<T> paramDef) {
        super(paramDef);
        this.endpoint = endpoint;
    }

    public EndpointParamDef(Endpoint endpoint, TypeReference<T> typeRef, String name) {
        super(typeRef, name);
        this.endpoint = endpoint;
    }

    public EndpointParamDef(Endpoint endpoint, Class<T> primitiveType, String name) {
        super(primitiveType, name);
        this.endpoint = endpoint;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public static <T> EndpointParamDef<T> of(String method, String pathPattern, String parameterName, TypeReference<T> parameterType) {
        return new EndpointParamDef<T>(method, pathPattern, parameterType, parameterName);
    }

    @Override
    public String toString() {
        return endpoint.toString() + " -> " + getType()+" "+getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointParamDef)) return false;
        if (!super.equals(o)) return false;
        EndpointParamDef<?> that = (EndpointParamDef<?>) o;
        return Objects.equal(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), endpoint);
    }
}
