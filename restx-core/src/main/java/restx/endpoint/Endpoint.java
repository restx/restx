package restx.endpoint;

import java.util.Objects;

/**
 * Created by fcamblor on 07/02/15.
 */
public class Endpoint {
    private final String method;
    private final String pathPattern;

    public Endpoint(String method, String pathPattern) {
        this.pathPattern = pathPattern;
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public static Endpoint of(String method, String pathPattern) {
        return new Endpoint(method, pathPattern);
    }

    @Override
    public String toString() {
        return method + " " + pathPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endpoint)) return false;
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(method, endpoint.method) &&
                Objects.equals(pathPattern, endpoint.pathPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, pathPattern);
    }
}
