package restx.factory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Set;

public class DeactivationFactoryMachine implements FactoryMachine {

	public static DeactivationFactoryMachine forNames(Iterable<Name<?>> names) {
		return new DeactivationFactoryMachine(names);
	}

	public static DeactivationFactoryMachine forNames(Name<?>... names) {
		return new DeactivationFactoryMachine(Lists.newArrayList(names));
	}

	private final ImmutableSet<String> keys;

	public DeactivationFactoryMachine(Iterable<Name<?>> keys) {
		this.keys = ImmutableSet.copyOf(Iterables.transform(keys, new Function<Name<?>, String>() {
			@Override
			public String apply(Name<?> input) {
				return Factory.activationKey(input.getClazz(), input.getName());
			}
		}));
	}

	@Override
	public boolean canBuild(Name<?> name) {
		return name.getClazz() == String.class && keys.contains(name.getName());
	}

	@Override
	public <T> MachineEngine<T> getEngine(Name<T> name) {
		return new NoDepsMachineEngine<T>(name, priority(), BoundlessComponentBox.FACTORY) {
			@Override
			protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
				return (T) "false";
			}
		};
	}

	@Override
	public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
		if (componentClass != String.class) {
			return ImmutableSet.of();
		}
		return Sets.newLinkedHashSet(Iterables.transform(keys, new Function<String, Name<T>>() {
			@Override
			public Name<T> apply(String input) {
				return (Name<T>) Name.of(String.class, input);
			}
		}));
	}

	@Override
	public int priority() {
		return -10000;
	}
}
