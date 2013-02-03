package restx;

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
        StringBuilder sb = new StringBuilder();
        toString(sb, "");
        return sb.toString();
    }

    public void toString(StringBuilder sb, String indent) {
        sb.append(indent).append(name).append("[RestxRouter] {\n");
        for (RestxRoute route : routes) {
            if (route instanceof RestxRouter) {
                ((RestxRouter) route).toString(sb, indent + "    ");
            } else {
                sb.append(indent).append("    ").append(route);
            }
            sb.append("\n");
        }
        sb.append(indent).append("}");
    }
}
