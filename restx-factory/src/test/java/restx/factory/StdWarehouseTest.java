package restx.factory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 6:34 PM
 */
public class StdWarehouseTest {

    @Test
    public void should_checkin_and_checkout_from_disposable_box() throws Exception {
        Warehouse warehouse = new StdWarehouse();

        warehouse.checkIn(new DisposableComponentBox<>(NamedComponent.of(String.class, "name", "test")),
                new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

        Optional<NamedComponent<String>> component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("name");
        assertThat(component.get().getComponent()).isEqualTo("test");

        component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent()).isFalse();
    }


    @Test
    public void should_checkin_and_checkout_from_boundless_box() throws Exception {
        Warehouse warehouse = new StdWarehouse();

        warehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
                new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

        Optional<NamedComponent<String>> component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("name");
        assertThat(component.get().getComponent()).isEqualTo("test");

        component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent()).isTrue();
        assertThat(component.get().getName().getName()).isEqualTo("name");
        assertThat(component.get().getComponent()).isEqualTo("test");
    }
}
