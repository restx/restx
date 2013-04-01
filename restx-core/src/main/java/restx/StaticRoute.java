package restx;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 10:42 PM
 */
public abstract class StaticRoute implements RestxRoute {
    private final String name;
    private final String httpMethod;
    private final String path;

    protected StaticRoute(String name, String httpMethod, String path) {
        this.name = name;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    @Override
    public Optional<RestxRouteMatch> match(RestxRequest req) {
        if (httpMethod.equals(req.getHttpMethod()) && path.equals(req.getRestxPath())) {
            return Optional.of(new RestxRouteMatch(this, path));
        } else {
            return Optional.absent();
        }
    }


    @Override
    public String toString() {
        return String.format("%s %s => %s", httpMethod, path, name);
    }

}
