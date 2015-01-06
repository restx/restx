package restx.config;

import static com.google.common.collect.Iterables.addAll;


import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.common.StdRestxConfig;
import restx.factory.BillOfMaterials;
import restx.factory.BoundlessComponentBox;
import restx.factory.Factory;
import restx.factory.FactoryMachine;
import restx.factory.Machine;
import restx.factory.MachineEngine;
import restx.factory.Name;
import restx.factory.NamedComponent;
import restx.factory.SatisfiedBOM;
import restx.factory.StdMachineEngine;

/**
 * User: xavierhanin
 * Date: 9/24/13
 * Time: 10:16 PM
 */
@Machine
public class ConsolidatedConfigFactoryMachine implements FactoryMachine {
    @Override
    public boolean canBuild(Name<?> name) {
        return name.getClazz() == RestxConfig.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MachineEngine<T> getEngine(Name<T> name) {
        if (!canBuild(name)) {
            throw new IllegalArgumentException("unsuported name " + name);
        }
        return (MachineEngine<T>) new StdMachineEngine<RestxConfig>((Name<RestxConfig>) name, priority(), BoundlessComponentBox.FACTORY) {

            private final Factory.Query<ConfigSupplier> configSupplierQuery = Factory.Query.byClass(ConfigSupplier.class);
            private final Factory.Query<String> stringsQuery = Factory.Query.byClass(String.class);

            @Override
            protected RestxConfig doNewComponent(SatisfiedBOM satisfiedBOM) {
                List<ConfigElement> elements = new ArrayList<>();

                // fetch system properties as ConfigElements, of strongest priority

                /* they are also available through named strings thanks to SystemPropertyFactoryMachine
                 * but fetching them here ensures they get highest priority and give clear indication of their origin.
                 * We could get rid of SystemPropertyFactoryMachine, but it may be helpful for someone who doesn't use
                 * RestxConfig at all.
                 */
                for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                    // System properties are a Map which is not protected against adding non String entries
                    // we simply ignore them
                    if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                        elements.add(ConfigElement.of("system", "", (String) entry.getKey(), (String) entry.getValue()));
                    }
                }

                // now fetch elements coming from ConfigSuppliers
                for (NamedComponent<ConfigSupplier> supplier : satisfiedBOM.get(configSupplierQuery)) {
                    addAll(elements, supplier.getComponent().get().elements());
                }
                RestxConfig config = StdRestxConfig.of(elements);

                // and now String components
                List<ConfigElement> factoryElements = new ArrayList<>();
                for (NamedComponent<String> s : satisfiedBOM.get(stringsQuery)) {
                    Optional<ConfigElement> element = config.getElement(s.getName().getName());
                    if (element.isPresent() && element.get().getValue().equals(s.getComponent())) {
                        // we don't add values from factory with the same value as the one found in config:
                        // it's probably because config elements are provided as String components too.
                        continue;
                    }
                    factoryElements.add(ConfigElement.of("factory", element.isPresent() ? element.get().getDoc() : "",
                            s.getName().getName(), s.getComponent()));
                }

                return StdRestxConfig.of(Iterables.concat(factoryElements, elements));
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(configSupplierQuery, stringsQuery);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        if (componentClass == RestxConfig.class) {
            return Collections.singleton(Name.of((Class<T>) RestxConfig.class));
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
        return ConsolidatedConfigFactoryMachine.class.getSimpleName();
    }
}
