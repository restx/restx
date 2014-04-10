package restx;

import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 7:54 AM
 */
public class StdRestxRequestMatch implements RestxRequestMatch {
    private final String pattern;
    private final String path;
    private final ImmutableMap<String, String> pathParams;
    private final ImmutableMap<String, ? extends Object> otherParams;

    public StdRestxRequestMatch(String path) {
        this(path, path, ImmutableMap.<String,String>of());
    }

    public StdRestxRequestMatch(String pattern, String path) {
        this(pattern, path, ImmutableMap.<String, String>of());
    }

    public StdRestxRequestMatch(String pattern, String path, ImmutableMap<String, String> pathParams) {
        this(pattern, path, pathParams, ImmutableMap.<String, Object>of());
    }

    public StdRestxRequestMatch(String pattern, String path,
                                ImmutableMap<String, String> pathParams,
                                ImmutableMap<String, ? extends Object> otherParams) {
        this.pattern = checkNotNull(pattern);
        this.path = checkNotNull(path);
        this.pathParams = checkNotNull(pathParams);
        this.otherParams = checkNotNull(otherParams);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getPathParam(String paramName) {
        String v = pathParams.get(paramName);
        if (v == null) {
            throw new IllegalStateException(
                    String.format("path parameter %s was not found", paramName));
        }
        return v;
    }

    @Override
    public ImmutableMap<String, String> getPathParams() {
        return pathParams;
    }

    @Override
    public ImmutableMap<String, ? extends Object> getOtherParams() {
        return otherParams;
    }

    @Override
    public String toString() {
        return "StdRestxHandlerMatch{" +
                "pattern='" + pattern + '\'' +
                ", path='" + path + '\'' +
                ", pathParams=" + pathParams +
                ", otherParams=" + otherParams +
                '}';
    }
}
