package restx.endpoint;

import com.google.common.base.Objects;
import restx.common.TypeReference;
import restx.factory.NamedType;

/**
 * Created by fcamblor on 07/02/15.
 */
public class EndpointParameter<T> {
    private final Endpoint endpoint;
    private final NamedType<T> parameter;

    public EndpointParameter(String method, String pathPattern, TypeReference<T> parameterType, String parameterName) {
        this(method, pathPattern, NamedType.of(parameterType, parameterName));
    }

    public EndpointParameter(String method, String pathPattern, NamedType<T> parameter) {
        this(new Endpoint(method, pathPattern), parameter);
    }

    public EndpointParameter(Endpoint endpoint, NamedType<T> parameter) {
        this.endpoint = endpoint;
        this.parameter = parameter;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public NamedType getParameter() {
        return parameter;
    }

    public static <T> EndpointParameter<T> of(String method, String pathPattern, String parameterName, TypeReference<T> parameterType) {
        return new EndpointParameter<T>(method, pathPattern, parameterType, parameterName);
    }

    @Override
    public String toString() {
        return endpoint.toString() + " -> " + parameter.getType()+" "+parameter.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndpointParameter)) return false;
        EndpointParameter that = (EndpointParameter) o;
        return Objects.equal(endpoint, that.endpoint) &&
                Objects.equal(parameter, that.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(endpoint, parameter);
    }
}
