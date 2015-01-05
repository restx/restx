package restx.factory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrap a warehouse in order to filter components by classes or names.
 * <p>
 * When request are made using {@link #checkOut(Name)}, {@link #listNames()}
 * or {@link #listDependencies(Name)} for a filtered name or class, the wrapper
 * would respond like if the component was not present.
 *
 * @author apeyrard
 */
public class FilteredWarehouse implements Warehouse {

	public static FilteredWarehouse forClasses(Warehouse original, Class<?>... classes) {
		return new FilteredWarehouse(original, ImmutableSet.copyOf(classes), ImmutableSet.<Name<?>>of());
	}

	public static FilteredWarehouse forNames(Warehouse original, Name<?>... names) {
		return new FilteredWarehouse(original, ImmutableSet.<Class<?>>of(), ImmutableSet.copyOf(names));
	}

	public static FilteredWarehouseBuilder builder(Warehouse original) {
		return new FilteredWarehouseBuilder(original);
	}

	private final ImmutableSet<Class<?>> filteredClasses;
	private final ImmutableSet<Name<?>> filteredNames;
	private final Warehouse original;

	private FilteredWarehouse(Warehouse original, Iterable<Class<?>> filteredClasses, Iterable<Name<?>> filteredNames) {
		this.filteredClasses = ImmutableSet.copyOf(filteredClasses);
		this.filteredNames = ImmutableSet.copyOf(filteredNames);
		this.original = original;
	}

	@Override
	public String getId() {
		return original.getId();
	}

	@Override
	public ImmutableList<Warehouse> getProviders() {
		return original.getProviders();
	}

	@Override
	public <T> Optional<StoredBox<T>> getStoredBox(Name<T> name) {
		if (isFiltered(name)) {
			return Optional.absent();
		}
		return original.getStoredBox(name);
	}

	@Override
	public <T> Optional<NamedComponent<T>> checkOut(Name<T> name) {
		if (isFiltered(name)) {
			return Optional.absent();
		}
		return original.checkOut(name);
	}

	@Override
	public <T> void checkIn(ComponentBox<T> componentBox, SatisfiedBOM satisfiedBOM) {
		original.checkIn(componentBox, satisfiedBOM);
	}

	@Override
	public Iterable<Name<?>> listNames() {
		return Iterables.filter(original.listNames(), new Predicate<Name<?>>() {
			@Override
			public boolean apply(Name<?> name) {
				return !isFiltered(name);
			}
		});
	}

	@Override
	public Iterable<Name<?>> listDependencies(Name name) {
		if (isFiltered(name)) {
			return Collections.emptySet();
		}
		return original.listDependencies(name);
	}

	@Override
	public void close() {
		original.close();
	}

	/**
	 * checks if the specified Name is filtered
	 */
	private boolean isFiltered(Name<?> name) {
		return filteredNames.contains(name) || filteredClasses.contains(name.getClazz());
	}

	/**
	 * Builder to create a custom {@link FilteredWarehouse}
	 */
	public static class FilteredWarehouseBuilder {
		private final Warehouse original;
		private final Set<Class<?>> filteredClasses = new HashSet<>();
		private final Set<Name<?>> filteredNames = new HashSet<>();

		private FilteredWarehouseBuilder(Warehouse original) {
			this.original = original;
		}

		public FilteredWarehouseBuilder addFilteredClass(Class<?> clazz) {
			filteredClasses.add(clazz);
			return this;
		}

		public FilteredWarehouseBuilder addFilteredName(Name<?> name) {
			filteredNames.add(name);
			return this;
		}

		public FilteredWarehouse build() {
			return new FilteredWarehouse(original, filteredClasses, filteredNames);
		}
	}
}
