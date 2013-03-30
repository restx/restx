package restx;

import com.google.common.collect.ImmutableList;

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
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        return ctx.withRoutes(routes).proceed(req, resp);
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

    public int getNbRoutes() {
        int count = 0;
        for (RestxRoute route : routes) {
            if (route instanceof RestxRouter) {
                count += ((RestxRouter) route).getNbRoutes();
            } else {
                count++;
            }
        }
        return count;
    }

    public ImmutableList<RestxRoute> getRoutes() {
        return routes;
    }
}
