package restx.converters;

import restx.factory.*;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 2:46 PM
*/
@Machine
public final class StringConvertersMachine extends SingleNameFactoryMachine<MainStringConverter> {
    public static final Name<MainStringConverter> NAME = Name.of(MainStringConverter.class, "MainStringConverter");

    public StringConvertersMachine() {
        super(0, new StdMachineEngine<MainStringConverter>(NAME, BoundlessComponentBox.FACTORY) {
            private Factory.Query<StringConverter> stringConverters = Factory.Query.byClass(StringConverter.class);

            @Override
            public MainStringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new MainStringConverter(satisfiedBOM.getAsComponents(stringConverters));
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(stringConverters);
            }
        });
    }
}
