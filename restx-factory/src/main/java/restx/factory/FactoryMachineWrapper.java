package restx.factory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:56 PM
 */
public class FactoryMachineWrapper implements FactoryMachine {
    public static Builder from(FactoryMachine factoryMachine) {
        return new Builder().from(factoryMachine);
    }

    public static class Builder {
        private FactoryMachine factoryMachine;
        private Integer priority;
        private Function transform;
        private List<Factory.Query<?>> deps = Lists.newArrayList();

        public Builder from(final FactoryMachine factoryMachine) {
            this.factoryMachine = factoryMachine;
            return this;
        }

        public Builder withPriority(final int priority) {
            this.priority = priority;
            return this;
        }

        public Builder withDependencies(Factory.Query<?>... q) {
            deps.addAll(Lists.newArrayList(q));
            return this;
        }

        public Builder transformComponents(final Function<Map.Entry<SatisfiedBOM, NamedComponent>, NamedComponent> transform) {
            this.transform = transform;
            return this;
        }

        public FactoryMachine build() {
            if (transform == null) {
                return new FactoryMachineWrapper(factoryMachine) {
                    @Override
                    public int priority() {
                        return priority == null ? super.priority() : priority;
                    }
                };
            }

            return new FactoryMachineWrapper(factoryMachine) {
                @Override
                public int priority() {
                    return priority == null ? super.priority() : priority;
                }

                @Override
                public <T> MachineEngine<T> getEngine(Name<T> name) {
                    return new MachineEngineWrapper<T>(super.getEngine(name)) {
                        @Override
                        public BillOfMaterials getBillOfMaterial() {
                            return super.getBillOfMaterial().addQueries(deps);
                        }

                        @Override
                        public ComponentBox<T> newComponent(final SatisfiedBOM satisfiedBOM) {
                            return new ComponentBoxWrapper<T>(super.newComponent(satisfiedBOM)) {
                                @Override
                                public Optional<NamedComponent<T>> pick() {
                                    Optional<NamedComponent<T>> pick = super.pick();
                                    if (!pick.isPresent()) {
                                        return pick;
                                    }
                                    return Optional.of((NamedComponent<T>) transform.apply(
                                            Maps.immutableEntry(satisfiedBOM, pick.get())));
                                }
                            };
                        }
                    };
                }
            };
        }
    }

    private final FactoryMachine original;

    public FactoryMachineWrapper(FactoryMachine original) {
        this.original = original;
    }

    public boolean canBuild(Name<?> name) {
        return original.canBuild(name);
    }

    public int priority() {
        return original.priority();
    }

    public <T> MachineEngine<T> getEngine(Name<T> name) {
        return original.getEngine(name);
    }

    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        return original.nameBuildableComponents(componentClass);
    }
}
