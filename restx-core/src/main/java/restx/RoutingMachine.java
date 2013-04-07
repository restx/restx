package restx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import restx.factory.*;

import java.util.Collection;

/**
 * User: xavierhanin
 * Date: 4/1/13
 * Time: 11:35 PM
 */
@Machine
public class RoutingMachine extends SingleNameFactoryMachine<RestxRouting> {
    public RoutingMachine() {
        super(0, new StdMachineEngine<RestxRouting>(Name.of(RestxRouting.class), BoundlessComponentBox.FACTORY) {
            private final Factory.Query<RestxFilter> filters = Factory.Query.byClass(RestxFilter.class);
            private final Factory.Query<RestxRouter> routers = Factory.Query.byClass(RestxRouter.class);
            private final Factory.Query<RestxRoute> routes = Factory.Query.byClass(RestxRoute.class);

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(filters, routers, routes);
            }

            @Override
            protected RestxRouting doNewComponent(SatisfiedBOM satisfiedBOM) {
                Collection<RestxRoute> r = Lists.newArrayList();
                for (RestxRouter router : satisfiedBOM.getAsComponents(routers)) {
                    r.addAll(router.getRoutes());
                }
                Iterables.addAll(r, satisfiedBOM.getAsComponents(routes));

                return new RestxRouting(ImmutableList.copyOf(satisfiedBOM.getAsComponents(filters)),
                        ImmutableList.copyOf(r));
            }
        });
    }
}
