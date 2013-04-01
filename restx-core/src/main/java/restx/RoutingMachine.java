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

            private Factory.Query<RestxFilter> filters;
            private Factory.Query<RestxRouter> routers;
            private Factory.Query<RestxRoute> routes;

            @Override
            public BillOfMaterials getBillOfMaterial() {
                filters = Factory.Query.byClass(RestxFilter.class);
                routers = Factory.Query.byClass(RestxRouter.class);
                routes = Factory.Query.byClass(RestxRoute.class);
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
