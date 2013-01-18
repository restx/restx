package restx;

import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 7:54 AM
 */
public class RestxRouteMatch {
    private final ImmutableMap<String, String> pathParams;
    private final String path;

    public RestxRouteMatch(String path, ImmutableMap<String, String> pathParams) {
        this.path = checkNotNull(path);
        this.pathParams = checkNotNull(pathParams);
    }

    public String getPath() {
        return path;
    }

    public ImmutableMap<String, String> getPathParams() {
        return pathParams;
    }

    @Override
    public String toString() {
        return "RestxRouteMatch{" +
                "pathParams=" + pathParams +
                ", path='" + path + '\'' +
                '}';
    }
}
