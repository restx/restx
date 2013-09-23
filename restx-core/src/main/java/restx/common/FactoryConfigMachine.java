package restx.common;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import restx.factory.*;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 9/23/13
 * Time: 9:16 AM
 */
@Machine
public class FactoryConfigMachine implements FactoryMachine {
    @Override
    public boolean canBuild(Name<?> name) {
        return name.getClazz() == Config.class;
    }

    @Override
    public <T> MachineEngine<T> getEngine(Name<T> name) {
        if (name.getClazz() != Config.class) {
            throw new IllegalArgumentException("unsupported name " + name);
        }
        return new StdMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {

            private Factory.Query<Factory> factoryQuery = Factory.Query.factoryQuery();

            @Override
            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                final Factory factory
                        = satisfiedBOM.getOneAsComponent(factoryQuery).get();
                Properties properties = new Properties();
                for (NamedComponent<String> s : factory.queryByClass(String.class).find()) {
                    properties.put(s.getName().getName(), s.getComponent());
                }
                for (NamedComponent<Integer> s : factory.queryByClass(Integer.class).find()) {
                    properties.put(s.getName().getName(), s.getComponent());
                }
                for (NamedComponent<Long> s : factory.queryByClass(Long.class).find()) {
                    properties.put(s.getName().getName(), s.getComponent());
                }
                for (NamedComponent<Boolean> s : factory.queryByClass(Boolean.class).find()) {
                    properties.put(s.getName().getName(), s.getComponent());
                }

                return (T) ConfigFactory.parseProperties(
                        properties, ConfigParseOptions.defaults().setOriginDescription("factory"))
                                .withFallback(ConfigFactory.load());
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(factoryQuery);
            }
        };
    }

    @Override
    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        if (componentClass == Config.class) {
            return Collections.singleton((Name<T>) Name.of(Config.class));
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public int priority() {
        return 0;
    }
}
