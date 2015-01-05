package restx.factory;

import static com.google.common.base.Preconditions.checkNotNull;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.Collections;

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
		FilteredWarehouseBuilder builder = builder(original);
		for (Class<?> clazz : classes) {
			builder.addFilteredClass(clazz);
		}
		return builder.build();
	}

	public static FilteredWarehouse forNames(Warehouse original, Name<?>... names) {
		FilteredWarehouseBuilder builder = builder(original);
		for (Name<?> name : names) {
			builder.addFilteredName(name);
		}
		return builder.build();
	}

	public static FilteredWarehouse forPredicate(Warehouse original, Predicate<Name<?>> filter) {
		return new FilteredWarehouse(original, filter);
	}

	public static FilteredWarehouseBuilder builder(Warehouse original) {
		return new FilteredWarehouseBuilder(original);
	}

	private final Predicate<Name<?>> filter;
	private final Warehouse original;

	private FilteredWarehouse(Warehouse original, Predicate<Name<?>> filter) {
		this.original = checkNotNull(original);
		this.filter = checkNotNull(filter);
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
		return filter.apply(name);
	}

	/**
	 * Builder to create a custom {@link FilteredWarehouse}
	 */
	public static class FilteredWarehouseBuilder {
		private final Warehouse original;
		private final ImmutableSet.Builder<Class<?>> filteredClassesBuilder = ImmutableSet.builder();
		private final ImmutableSet.Builder<Name<?>> filteredNamesBuilder = ImmutableSet.builder();
		private final ImmutableList.Builder<Predicate<Name<?>>> predicatesBuilder = ImmutableList.builder();

		private FilteredWarehouseBuilder(Warehouse original) {
			this.original = original;
		}

		public FilteredWarehouseBuilder addFilteredClass(Class<?> clazz) {
			filteredClassesBuilder.add(clazz);
			return this;
		}

		public FilteredWarehouseBuilder addFilteredName(Name<?> name) {
			filteredNamesBuilder.add(name);
			return this;
		}

		public FilteredWarehouseBuilder addPredicate(Predicate<Name<?>> predicate) {
			predicatesBuilder.add(predicate);
			return this;
		}

		public FilteredWarehouse build() {
			final ImmutableSet<Class<?>> filteredClasses = filteredClassesBuilder.build();
			final ImmutableSet<Name<?>> filteredNames = filteredNamesBuilder.build();
			final ImmutableList<Predicate<Name<?>>> predicates = predicatesBuilder.build();

			return new FilteredWarehouse(
					original,
					new Predicate<Name<?>>() {
						@Override
						public boolean apply(Name<?> name) {
							return filteredClasses.contains(name.getClazz()) ||
									filteredNames.contains(name) ||
									Predicates.or(predicates).apply(name);
						}
					}
			);
		}
	}
}
