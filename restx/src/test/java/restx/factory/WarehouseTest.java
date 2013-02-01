package restx.factory;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * User: xavierhanin
 * Date: 1/31/13
 * Time: 6:34 PM
 */
public class WarehouseTest {

    @Test
    public void should_checkin_and_checkout_from_disposable_box() throws Exception {
        Warehouse warehouse = new Warehouse();

        warehouse.checkIn(new DisposableComponentBox<>(NamedComponent.of(String.class, "name", "test")));

        Optional<NamedComponent<String>> component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent(), equalTo(true));
        assertThat(component.get().getName().getName(), equalTo("name"));
        assertThat(component.get().getComponent(), equalTo("test"));

        component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent(), equalTo(false));
    }


    @Test
    public void should_checkin_and_checkout_from_boundless_box() throws Exception {
        Warehouse warehouse = new Warehouse();

        warehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")));

        Optional<NamedComponent<String>> component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent(), equalTo(true));
        assertThat(component.get().getName().getName(), equalTo("name"));
        assertThat(component.get().getComponent(), equalTo("test"));

        component = warehouse.checkOut(Name.of(String.class, "name"));
        assertThat(component.isPresent(), equalTo(true));
        assertThat(component.get().getName().getName(), equalTo("name"));
        assertThat(component.get().getComponent(), equalTo("test"));
    }
}
