package restx.factory;

import static org.assertj.core.api.Assertions.assertThat;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

/**
 * @author apeyrard
 */
public class FilteredWarehouseTest {

	@Test
	public void should_filter_by_names_for_checkout() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some string components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// assert that the components can be retrieve from the original warehouse
		Optional<NamedComponent<String>> component = originalWarehouse.checkOut(Name.of(String.class, "name"));
		assertThat(component.isPresent()).isTrue();
		assertThat(component.get().getName().getName()).isEqualTo("name");
		assertThat(component.get().getComponent()).isEqualTo("test");
		component = originalWarehouse.checkOut(Name.of(String.class, "name2"));
		assertThat(component.isPresent()).isTrue();
		assertThat(component.get().getName().getName()).isEqualTo("name2");
		assertThat(component.get().getComponent()).isEqualTo("test2");

		// create a filtered warehouse
		Warehouse filteredWarehouse = FilteredWarehouse.excludingNames(originalWarehouse, Name.of(String.class, "name"));

		// try to checkout from filtered warehouse, the filtered component must not be present
		component = filteredWarehouse.checkOut(Name.of(String.class, "name"));
		assertThat(component.isPresent()).isFalse();

		// but the other component, must still be available
		component = filteredWarehouse.checkOut(Name.of(String.class, "name2"));
		assertThat(component.isPresent()).isTrue();
	}

	@Test
	public void should_filter_by_names_for_listNames() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some string components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// create a filtered warehouse
		Warehouse filteredWarehouse = FilteredWarehouse.excludingNames(originalWarehouse, Name.of(String.class, "name"));

		// try to list available names in the warehouse
		assertThat(originalWarehouse.listNames()).containsOnly(Name.of(String.class, "name"), Name.of(String.class, "name2"));
		assertThat(filteredWarehouse.listNames()).containsExactly(Name.of(String.class, "name2"));
	}

	@Test
	public void should_filter_by_names_for_getStoredBoxes() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some string components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// create a filtered warehouse
		Warehouse filteredWarehouse = FilteredWarehouse.excludingNames(originalWarehouse, Name.of(String.class, "name"));

		// try to get the box for the filtered component
		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isTrue();
		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isFalse();

		// the box for the not filtered component must be available
		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isTrue();
		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isTrue();
	}

	@Test
	public void should_filter_by_classes_for_checkout() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "answer", 42)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));


		// assert that the components can be retrieve from the original warehouse
		Optional<NamedComponent<String>> component = originalWarehouse.checkOut(Name.of(String.class, "name"));
		assertThat(component.isPresent()).isTrue();
		assertThat(component.get().getName().getName()).isEqualTo("name");
		assertThat(component.get().getComponent()).isEqualTo("test");
		component = originalWarehouse.checkOut(Name.of(String.class, "name2"));
		assertThat(component.isPresent()).isTrue();
		assertThat(component.get().getName().getName()).isEqualTo("name2");
		assertThat(component.get().getComponent()).isEqualTo("test2");
		Optional<NamedComponent<Integer>> answer = originalWarehouse.checkOut(Name.of(Integer.class, "answer"));
		assertThat(answer.isPresent()).isTrue();
		assertThat(answer.get().getName().getName()).isEqualTo("answer");
		assertThat(answer.get().getComponent()).isEqualTo(42);

		// create a filtered warehouse
		Warehouse filteredWarehouse = FilteredWarehouse.excludingClasses(originalWarehouse, String.class);

		// try to checkout from filtered warehouse, no component must be present
		component = filteredWarehouse.checkOut(Name.of(String.class, "name"));
		assertThat(component.isPresent()).isFalse();
		component = filteredWarehouse.checkOut(Name.of(String.class, "name2"));
		assertThat(component.isPresent()).isFalse();

		// the integer component must be available
		answer = filteredWarehouse.checkOut(Name.of(Integer.class, "answer"));
		assertThat(answer.isPresent()).isTrue();
	}

	@Test
	public void should_filter_by_classes_for_listNames() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "answer", 42)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// create a filtered warehouse
		Warehouse filteredWarehouse = FilteredWarehouse.excludingClasses(originalWarehouse, String.class);

		// try to list available names in the warehouse
		assertThat(originalWarehouse.listNames()).containsOnly(
				Name.of(String.class, "name"),
				Name.of(String.class, "name2"),
				Name.of(Integer.class, "answer")
		);
		assertThat(filteredWarehouse.listNames()).containsExactly(Name.of(Integer.class, "answer"));
	}

	@Test
	public void should_filter_by_classes_for_getStoredBoxes() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some string components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "answer", 42)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// create a filtered warehouse
		Warehouse filteredWarehouse = FilteredWarehouse.excludingClasses(originalWarehouse, String.class);

		// both boxes for string component must not be available from the filtered warehouse
		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isTrue();
		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isFalse();
		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isFalse();

		// but the answer must still be available
		assertThat(originalWarehouse.getStoredBox(Name.of(Integer.class, "answer")).isPresent()).isTrue();
		assertThat(filteredWarehouse.getStoredBox(Name.of(Integer.class, "answer")).isPresent()).isTrue();
	}

	@Test
	public void should_filter_by_custom_filters_for_checkOut() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "hello world", 1)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "foo", 2)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "answer", 42)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "devil", 666)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// filter strings and answer, only devil will not be filtered
		FilteredWarehouse filteredWarehouse = FilteredWarehouse.builder(originalWarehouse)
				.excludingClasses(String.class)
				.excludingNames(Name.of(Integer.class, "answer"))
				.excluding(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return name.getName().contains("hello");
					}
				})
				.excluding(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return name.getName().startsWith("f");
					}
				})
				.build();

		assertThat(originalWarehouse.checkOut(Name.of(String.class, "name")).isPresent()).isTrue();
		assertThat(originalWarehouse.checkOut(Name.of(String.class, "name2")).isPresent()).isTrue();
		assertThat(originalWarehouse.checkOut(Name.of(Integer.class, "hello world")).isPresent()).isTrue();
		assertThat(originalWarehouse.checkOut(Name.of(Integer.class, "foo")).isPresent()).isTrue();
		assertThat(originalWarehouse.checkOut(Name.of(Integer.class, "answer")).isPresent()).isTrue();
		assertThat(originalWarehouse.checkOut(Name.of(Integer.class, "devil")).isPresent()).isTrue();

		assertThat(filteredWarehouse.checkOut(Name.of(String.class, "name")).isPresent()).isFalse();
		assertThat(filteredWarehouse.checkOut(Name.of(String.class, "name2")).isPresent()).isFalse();
		assertThat(filteredWarehouse.checkOut(Name.of(Integer.class, "hello world")).isPresent()).isFalse();
		assertThat(filteredWarehouse.checkOut(Name.of(Integer.class, "foo")).isPresent()).isFalse();
		assertThat(filteredWarehouse.checkOut(Name.of(Integer.class, "answer")).isPresent()).isFalse();
		assertThat(filteredWarehouse.checkOut(Name.of(Integer.class, "devil")).isPresent()).isTrue();
	}

	@Test
	public void should_filter_by_custom_filters_for_listNames() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "hello world", 1)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "foo", 2)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "answer", 42)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "devil", 666)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// filter strings and answer, only devil will not be filtered
		FilteredWarehouse filteredWarehouse = FilteredWarehouse.builder(originalWarehouse)
				.excludingClasses(String.class)
				.excludingNames(Name.of(Integer.class, "answer"))
				.excluding(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return name.getName().contains("hello");
					}
				})
				.excluding(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return name.getName().startsWith("f");
					}
				})
				.build();

		assertThat(originalWarehouse.listNames()).containsOnly(
				Name.of(String.class, "name"),
				Name.of(String.class, "name2"),
				Name.of(Integer.class, "hello world"),
				Name.of(Integer.class, "foo"),
				Name.of(Integer.class, "answer"),
				Name.of(Integer.class, "devil")
		);

		assertThat(filteredWarehouse.listNames()).containsOnly(
				Name.of(Integer.class, "devil")
		);
	}

	@Test
	public void should_filter_by_custom_filters_for_getStoredBox() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "hello world", 1)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "foo", 2)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "answer", 42)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "devil", 666)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// filter strings and answer, only devil will not be filtered
		FilteredWarehouse filteredWarehouse = FilteredWarehouse.builder(originalWarehouse)
				.excludingClasses(String.class)
				.excludingNames(Name.of(Integer.class, "answer"))
				.excluding(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return name.getName().contains("hello");
					}
				})
				.excluding(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return name.getName().startsWith("f");
					}
				})
				.build();

		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(Integer.class, "hello world")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(Integer.class, "foo")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(Integer.class, "answer")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(Integer.class, "devil")).isPresent()).isTrue();

		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isFalse();
		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isFalse();
		assertThat(filteredWarehouse.getStoredBox(Name.of(Integer.class, "hello world")).isPresent()).isFalse();
		assertThat(filteredWarehouse.getStoredBox(Name.of(Integer.class, "foo")).isPresent()).isFalse();
		assertThat(filteredWarehouse.getStoredBox(Name.of(Integer.class, "answer")).isPresent()).isFalse();
		assertThat(filteredWarehouse.getStoredBox(Name.of(Integer.class, "devil")).isPresent()).isTrue();
	}

	@Test
	public void should_filter_by_predicate_for_checkout() {
		// create a warehouse
		Warehouse originalWarehouse = new StdWarehouse();

		// check in some components
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name", "test")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(String.class, "name2", "test2")),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "answer", 42)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));
		originalWarehouse.checkIn(new BoundlessComponentBox<>(NamedComponent.of(Integer.class, "devil", 666)),
				new SatisfiedBOM(BillOfMaterials.EMPTY, ImmutableMultimap.<Factory.Query<?>, NamedComponent<?>>of()));

		// filter strings and answer, only devil will not be filtered
		FilteredWarehouse filteredWarehouse = FilteredWarehouse.excluding(
				originalWarehouse,
				new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return name.getName().length() == 5;
					}
				}
		);

		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(Integer.class, "answer")).isPresent()).isTrue();
		assertThat(originalWarehouse.getStoredBox(Name.of(Integer.class, "devil")).isPresent()).isTrue();

		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name")).isPresent()).isTrue();
		assertThat(filteredWarehouse.getStoredBox(Name.of(String.class, "name2")).isPresent()).isFalse();
		assertThat(filteredWarehouse.getStoredBox(Name.of(Integer.class, "answer")).isPresent()).isTrue();
		assertThat(filteredWarehouse.getStoredBox(Name.of(Integer.class, "devil")).isPresent()).isFalse();

	}
}
