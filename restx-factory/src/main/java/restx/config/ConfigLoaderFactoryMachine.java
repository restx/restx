package restx.config;

import com.google.common.collect.ImmutableSet;

import restx.factory.BillOfMaterials;
import restx.factory.BoundlessComponentBox;
import restx.factory.Factory;
import restx.factory.Machine;
import restx.factory.Name;
import restx.factory.SatisfiedBOM;
import restx.factory.SingleNameFactoryMachine;
import restx.factory.StdMachineEngine;

/**
 * Machine for {@link ConfigLoader}.
 */
@Machine
public class ConfigLoaderFactoryMachine extends SingleNameFactoryMachine<ConfigLoader> {
    public static final Name<ConfigLoader> NAME = Name.of(ConfigLoader.class, "ConfigLoader");

    public ConfigLoaderFactoryMachine() {
        super(0, new StdMachineEngine<ConfigLoader>(NAME, 0, BoundlessComponentBox.FACTORY) {
            private final Factory.Query<String> env = Factory.Query.byName(Name.of(String.class, "env")).optional();

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(
                        env
                ));
            }

            @Override
            protected ConfigLoader doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new ConfigLoader(
                        satisfiedBOM.getOneAsComponent(env)
                );
            }
        });
    }
}
