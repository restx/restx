package restx.factory;

import com.google.common.collect.ImmutableSet;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 1:17 PM
 */
public class BillOfMaterials {
    public static final BillOfMaterials EMPTY = new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of());

    public static BillOfMaterials of(Factory.Query<?>... query) {
        return new BillOfMaterials(ImmutableSet.copyOf(query));
    }

    private final ImmutableSet<Factory.Query<?>> bill;

    public BillOfMaterials(ImmutableSet<Factory.Query<?>> bill) {
        this.bill = bill;
    }

    public ImmutableSet<Factory.Query<?>> getQueries() {
        return bill;
    }

    @Override
    public String toString() {
        return "BillOfMaterials{" + bill + '}';
    }
}
