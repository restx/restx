package restx;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 7:54 AM
 */
public class StdRestxRouteMatch implements RestxRouteMatch {
    private final RestxHandler handler;
    private final String pattern;
    private final String path;
    private final ImmutableMap<String, String> pathParams;
    private final ImmutableMap<String, ? extends Object> otherParams;

    public StdRestxRouteMatch(RestxHandler handler, String path) {
        this(handler, path, path, ImmutableMap.<String,String>of());
    }

    public StdRestxRouteMatch(RestxHandler handler, String pattern, String path) {
        this(handler, pattern, path, ImmutableMap.<String, String>of());
    }

    public StdRestxRouteMatch(RestxHandler handler, String pattern, String path, ImmutableMap<String, String> pathParams) {
        this(handler, pattern, path, pathParams, ImmutableMap.<String, Object>of());
    }

    public StdRestxRouteMatch(RestxHandler handler, String pattern, String path,
                              ImmutableMap<String, String> pathParams,
                              ImmutableMap<String, ? extends Object> otherParams) {
        this.handler = checkNotNull(handler);
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
    public ImmutableMap<String, String> getPathParams() {
        return pathParams;
    }

    @Override
    public ImmutableMap<String, ? extends Object> getOtherParams() {
        return otherParams;
    }

    @Override
    public void handle(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        handler.handle(this, req, resp, ctx);
    }

    @Override
    public String toString() {
        return "StdRestxRouteMatch{" +
                "handler=" + handler +
                ", pattern='" + pattern + '\'' +
                ", path='" + path + '\'' +
                ", pathParams=" + pathParams +
                ", otherParams=" + otherParams +
                '}';
    }
}
