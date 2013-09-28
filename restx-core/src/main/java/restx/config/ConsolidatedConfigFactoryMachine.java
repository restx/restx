package restx.config;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.common.StdRestxConfig;
import restx.factory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.addAll;

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
    public <T> MachineEngine<T> getEngine(Name<T> name) {
        if (!canBuild(name)) {
            throw new IllegalArgumentException("unsuported name " + name);
        }
        return new StdMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {

            private final Factory.Query<ConfigSupplier> configSupplierQuery = Factory.Query.byClass(ConfigSupplier.class);
            private final Factory.Query<String> stringsQuery = Factory.Query.byClass(String.class);

            @Override
            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                List<ConfigElement> elements = new ArrayList<>();

                for (NamedComponent<ConfigSupplier> supplier : satisfiedBOM.get(configSupplierQuery)) {
                    addAll(elements, supplier.getComponent().get().elements());
                }
                RestxConfig config = StdRestxConfig.of(elements);

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

                return (T) StdRestxConfig.of(Iterables.concat(factoryElements, elements));
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(configSupplierQuery, stringsQuery);
            }
        };
    }

    @Override
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
