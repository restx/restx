package restx.factory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.*;
import restx.annotations.RestxResource;
import restx.common.Tpl;
import restx.converters.StringConverter;

import java.io.IOException;
import java.util.List;

public class WarehouseRoute implements RestxRoute {
    private final Warehouse warehouse;

    public WarehouseRoute(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        if ("GET".equals(req.getHttpMethod()) && "/@/warehouse".equals(req.getRestxPath())) {
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

            Tpl tpl = new Tpl(WarehouseRoute.class, "factory.html");
            resp.getWriter().print(tpl.bind(ImmutableMap.of(
                    "nodes", Joiner.on("\n").join(nodesCode),
                    "links", Joiner.on("\n").join(linksCode))));
            return true;
        }

        return false;
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

    @Override
    public String toString() {
        return "GET /@/warehouse => WarehouseRoute";
    }

    public static class Machine extends SingleNameFactoryMachine<WarehouseRoute> {

        private static final Factory.Query<Factory> factoryQuery = Factory.Query.factoryQuery();

        public Machine() {
            super(0, new StdMachineEngine<WarehouseRoute>(Name.of(WarehouseRoute.class),
                    BoundlessComponentBox.FACTORY) {
                @Override
                protected WarehouseRoute doNewComponent(SatisfiedBOM satisfiedBOM) {
                    return new WarehouseRoute(satisfiedBOM.getOne(factoryQuery).get().getComponent().getWarehouse());
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return BillOfMaterials.of(factoryQuery);
                }
            });
        }
    }
}
