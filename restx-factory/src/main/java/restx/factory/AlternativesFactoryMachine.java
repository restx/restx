package restx.factory;

import com.google.common.collect.ImmutableMap;

public class AlternativesFactoryMachine<T> extends SingleNameFactoryMachine<FactoryMachine> {
    public AlternativesFactoryMachine(int priority,
                                      final Name<T> name,
                                      final ImmutableMap<T, ? extends FactoryMachine> alternatives,
                                      ComponentBox.BoxFactory boxFactory) {
        super(priority, new StdMachineEngine<FactoryMachine>(
                Name.of(FactoryMachine.class, name.getName() + "Alternatives"), priority, boxFactory) {
            private Factory.Query<T> query = Factory.Query.byName(name);

            @Override
            protected FactoryMachine doNewComponent(SatisfiedBOM satisfiedBOM) {
                FactoryMachine factoryMachine = alternatives.get(satisfiedBOM.getOne(query).get().getComponent());
                if (factoryMachine != null) {
                    return factoryMachine;
                } else {
                    return NoopFactoryMachine.INSTANCE;
                }
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(query);
            }
        });
    }
}
