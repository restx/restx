package restx;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:19 AM
 */
public class RestxRouter implements RestxRoute {
    private final ImmutableList<RestxRoute> routes;
    private final String name;

    public RestxRouter(String name, RestxRoute... routes) {
        this(name, ImmutableList.copyOf(routes));
    }

    public RestxRouter(String name, ImmutableList<RestxRoute> routes) {
        this.name = checkNotNull(name);
        this.routes = checkNotNull(routes);
    }

    @Override
    public boolean route(HttpServletRequest req, HttpServletResponse resp, RouteLifecycleListener listener) throws IOException {
        for (RestxRoute router : routes) {
            if (router.route(req, resp, listener)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name + "[RestxRouter] {\n\t" + Joiner.on("\n\t").join(routes) + "\n}";
    }
}
