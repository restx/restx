package restx.factory;

import com.github.mustachejava.Mustache;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.*;
import restx.annotations.RestxResource;
import restx.converters.StringConverter;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static restx.common.Mustaches.compile;

@Component
public class WarehouseRoute extends StdRoute {
    private final Warehouse warehouse;

    @Inject
    public WarehouseRoute(Factory factory) {
        this(factory.getWarehouse());
    }

    public WarehouseRoute(Warehouse warehouse) {
        super("WarehouseRoute", new StdRouteMatcher("GET", "/@/warehouse"));
        this.warehouse = warehouse;
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setContentType("text/html");

        List<String> nodesCode = Lists.newArrayList();
        List<String> linksCode = Lists.newArrayList();
        for (Name<?> name : warehouse.listNames()) {
            nodesCode.add(String.format("graph.addNode('%s', { name: '%s', type: '%s' });", name.asId(), name.getSimpleName(), getType(name)));
            Iterable<Name<?>> deps = warehouse.listDependencies(name);
            for (Name<?> dep : deps) {
                linksCode.add(String.format("graph.addLink('%s', '%s');", name.asId(), dep.asId()));
            }
        }

        Mustache tpl = compile(WarehouseRoute.class, "factory.mustache");
        tpl.execute(resp.getWriter(), ImmutableMap.of(
                "nodes", Joiner.on("\n").join(nodesCode),
                "links", Joiner.on("\n").join(linksCode)));
    }

    private String getType(Name<?> name) {
        if (RestxRouter.class.isAssignableFrom(name.getClazz())) {
            return RestxRouter.class.getSimpleName();
        }
        if (RestxRoute.class.isAssignableFrom(name.getClazz())) {
            return RestxRoute.class.getSimpleName();
        }
        if (StringConverter.class.isAssignableFrom(name.getClazz())) {
            return StringConverter.class.getSimpleName();
        }
        if (name.getClazz().getName().endsWith("Resource")) {
            return RestxResource.class.getSimpleName();
        }
        return name.getClazz().getSimpleName();
    }
}
