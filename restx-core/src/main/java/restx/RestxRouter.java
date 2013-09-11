package restx;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:19 AM
 */
public class RestxRouter {
    private final ImmutableList<RestxRoute> routes;
    private final String name;
    private final String groupName;

    public RestxRouter(String name, RestxRoute... routes) {
        this("default", name, routes);
    }

    public RestxRouter(String groupName, String name, RestxRoute... routes) {
        this(groupName, name, ImmutableList.copyOf(routes));
    }

    public RestxRouter(String groupName, String name, ImmutableList<RestxRoute> routes) {
        this.groupName = checkNotNull(groupName);
        this.name = checkNotNull(name);
        this.routes = checkNotNull(routes);
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
            sb.append(indent).append("    ").append(route).append("\n");
        }
        sb.append(indent).append("}");
    }

    public int getNbRoutes() {
        return routes.size();
    }

    public String getGroupName() {
        return groupName;
    }

    public ImmutableList<RestxRoute> getRoutes() {
        return routes;
    }

    public String getName() {
        return name;
    }
}
