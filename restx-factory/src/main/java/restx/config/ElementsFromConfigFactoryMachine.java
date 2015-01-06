package restx.config;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;
import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.factory.BillOfMaterials;
import restx.factory.BoundlessComponentBox;
import restx.factory.DefaultFactoryMachine;
import restx.factory.Factory;
import restx.factory.FactoryMachine;
import restx.factory.Machine;
import restx.factory.MachineEngine;
import restx.factory.Name;
import restx.factory.NoDepsMachineEngine;
import restx.factory.SatisfiedBOM;
import restx.factory.StdMachineEngine;

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
                        Optional<ConfigElement> configElement = config.getElement(name.getName());
                        return configElement.isPresent()
                            && (name.getClazz() == ConfigElement.class
                                || (name.getClazz() == String.class
                                    && !Strings.isNullOrEmpty(configElement.get().getValue()))
                              );
                    }

                    @Override
                    public <T> MachineEngine<T> getEngine(final Name<T> name) {
                        return new NoDepsMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {
                            @Override
                            @SuppressWarnings("unchecked")
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
                    @SuppressWarnings("unchecked")
                    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
                        if (String.class == componentClass) {
                            return (Set) Sets.newHashSet(Iterables.transform(config.elements(),
                                    new Function<ConfigElement, Name<String>>() {
                                @Override
                                public Name<String> apply(ConfigElement input) {
                                    return Name.of(String.class, input.getKey());
                                }
                            }));
                        } else if (ConfigElement.class == componentClass) {
                            return (Set) Sets.newHashSet(Iterables.transform(config.elements(),
                                    new Function<ConfigElement, Name<ConfigElement>>() {
                                @Override
                                public Name<ConfigElement> apply(ConfigElement input) {
                                    return Name.of(ConfigElement.class, input.getKey());
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

                    @Override
                    public String toString() {
                        return "ConfigFactoryMachine";
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
