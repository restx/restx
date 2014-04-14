package restx.factory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import restx.*;
import restx.annotations.RestxResource;

import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Component
public class WarehouseRoute extends StdRoute {
    private final Warehouse warehouse;

    @Inject
    public WarehouseRoute(Factory factory) {
        this(factory.getWarehouse());
    }

    public WarehouseRoute(Warehouse warehouse) {
        super("WarehouseRoute", new StdRestxRequestMatcher("GET", "/@/warehouse"));
        this.warehouse = warehouse;
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setContentType("application/json");

        List<String> nodesCode = Lists.newArrayList();
        List<String> linksCode = Lists.newArrayList();
        for (Name<?> name : warehouse.listNames()) {
            nodesCode.add(String.format("{ \"id\": \"%s\", \"name\": \"%s\", \"type\": \"%s\" }", name.asId(), name.getSimpleName(), getType(name)));
            Iterable<Name<?>> deps = warehouse.listDependencies(name);
            for (Name<?> dep : deps) {
                linksCode.add(String.format("{ \"origin\": \"%s\", \"target\": \"%s\" }", name.asId(), dep.asId()));
            }
        }

        PrintWriter writer = resp.getWriter();
        writer.println("{");
        writer.println("\"nodes\": [");
        Joiner.on(",\n").appendTo(writer, nodesCode);
        writer.println("\n],");
        writer.println("\"links\": [");
        Joiner.on(",\n").appendTo(writer, linksCode);
        writer.println("\n]");
        writer.println("}");
    }

    private String getType(Name<?> name) {
        if (RestxRouter.class.isAssignableFrom(name.getClazz())) {
            return RestxRouter.class.getSimpleName();
        }
        if (RestxRoute.class.isAssignableFrom(name.getClazz())) {
            return RestxRoute.class.getSimpleName();
        }
        if (name.getClazz().getName().endsWith("Resource")) {
            return RestxResource.class.getSimpleName();
        }
        return name.getClazz().getSimpleName();
    }
}
