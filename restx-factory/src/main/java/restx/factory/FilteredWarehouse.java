package restx.factory;

import static com.google.common.base.Preconditions.checkNotNull;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

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

	public static FilteredWarehouse excludingClasses(Warehouse original, Class<?>... classes) {
		return builder(original).excludingClasses(classes).build();
	}

	public static FilteredWarehouse excludingClasses(Warehouse original, Iterable<Class<?>> classes) {
		return builder(original).excludingClasses(classes).build();
	}


	public static FilteredWarehouse excludingNames(Warehouse original, Name<?>... names) {
		return builder(original).excludingNames(names).build();
	}

	public static FilteredWarehouse excludingNames(Warehouse original, Iterable<Name<?>> names) {
		return builder(original).excludingNames(names).build();
	}

	@SafeVarargs
	public static FilteredWarehouse excluding(Warehouse original, Predicate<Name<?>>... filters) {
		return builder(original).excluding(filters).build();
	}

	public static FilteredWarehouse excluding(Warehouse original, Iterable<Predicate<Name<?>>> filters) {
		return builder(original).excluding(filters).build();
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
		private final ImmutableList.Builder<Predicate<Name<?>>> predicatesBuilder = ImmutableList.builder();

		private FilteredWarehouseBuilder(Warehouse original) {
			this.original = original;
		}

		public FilteredWarehouseBuilder excludingClasses(Class<?>... classes) {
			return excludingClasses(ImmutableSet.copyOf(classes));
		}

		public FilteredWarehouseBuilder excludingClasses(Iterable<Class<?>> classes) {
			final ImmutableSet<Class<?>> classesSet = ImmutableSet.copyOf(classes);
			if (!classesSet.isEmpty()) {
				predicatesBuilder.add(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return classesSet.contains(name.getClazz());
					}
				});
			}
			return this;
		}

		public FilteredWarehouseBuilder excludingNames(Name<?>... names) {
			return excludingNames(ImmutableSet.copyOf(names));
		}

		public FilteredWarehouseBuilder excludingNames(Iterable<Name<?>> names) {
			final ImmutableSet<Name<?>> namesSet = ImmutableSet.copyOf(names);
			if (!namesSet.isEmpty()) {
				predicatesBuilder.add(new Predicate<Name<?>>() {
					@Override
					public boolean apply(Name<?> name) {
						return namesSet.contains(name);
					}
				});
			}
			return this;
		}

		@SafeVarargs
		public final FilteredWarehouseBuilder excluding(Predicate<Name<?>>... predicates) {
			predicatesBuilder.add(predicates);
			return this;
		}

		public FilteredWarehouseBuilder excluding(Iterable<Predicate<Name<?>>> predicates) {
			predicatesBuilder.addAll(predicates);
			return this;
		}

		public FilteredWarehouse build() {
			final ImmutableList<Predicate<Name<?>>> predicates = predicatesBuilder.build();

			return new FilteredWarehouse(
					original,
					new Predicate<Name<?>>() {
						@Override
						public boolean apply(Name<?> name) {
							return Predicates.or(predicates).apply(name);
						}
					}
			);
		}
	}
}
