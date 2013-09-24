package restx.common;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import restx.factory.*;

import java.util.Collections;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 9/24/13
 * Time: 11:12 PM
 */
@Machine
public class ElementsFromConfigFactoryMachine extends DefaultFactoryMachine {

    public ElementsFromConfigFactoryMachine() {
        super(0, new StdMachineEngine<FactoryMachine>(
                Name.of(FactoryMachine.class, "ElementsFromConfig"), BoundlessComponentBox.FACTORY) {

            private Factory.Query<RestxConfig> restxConfigQuery = Factory.Query.byClass(RestxConfig.class).mandatory();

            @Override
            protected FactoryMachine doNewComponent(SatisfiedBOM satisfiedBOM) {
                final RestxConfig config = satisfiedBOM.getOneAsComponent(restxConfigQuery).get();
                return new FactoryMachine() {
                    @Override
                    public boolean canBuild(Name<?> name) {
                        return (name.getClazz() == String.class || name.getClazz() == ConfigElement.class)
                                && config.getElement(name.getName()).isPresent();
                    }

                    @Override
                    public <T> MachineEngine<T> getEngine(final Name<T> name) {
                        return new NoDepsMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {
                            @Override
                            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                                if (name.getClazz() == String.class) {
                                    return (T) config.getString(name.getName()).get();
                                }
                                if (name.getClazz() == ConfigElement.class) {
                                    return (T) config.getElement(name.getName()).get();
                                }
                                throw new IllegalArgumentException("can't satisfy name " + name);
                            }
                        };
                    }

                    @Override
                    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
                        if (String.class == componentClass) {
                            return Sets.newHashSet(Iterables.transform(config.elements(), new Function<ConfigElement, Name<T>>() {
                                @Override
                                public Name<T> apply(ConfigElement input) {
                                    return (Name<T>) Name.of(String.class, input.getKey());
                                }
                            }));
                        } else if (ConfigElement.class == componentClass) {
                            return Sets.newHashSet(Iterables.transform(config.elements(), new Function<ConfigElement, Name<T>>() {
                                @Override
                                public Name<T> apply(ConfigElement input) {
                                    return (Name<T>) Name.of(ConfigElement.class, input.getKey());
                                }
                            }));
                        } else {
                            return Collections.emptySet();
                        }
                    }

                    @Override
                    public int priority() {
                        return 0;
                    }
                };
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(restxConfigQuery);
            }
        });
    }
}
