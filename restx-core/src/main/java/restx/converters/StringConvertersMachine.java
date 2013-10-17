package restx.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import restx.factory.*;
import restx.jackson.FrontObjectMapperFactory;

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
            private Factory.Query<ObjectMapper> objectMapper = Factory.Query.byName(Name.of(ObjectMapper.class, FrontObjectMapperFactory.MAPPER_NAME));

            @Override
            public MainStringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new MainStringConverter(satisfiedBOM.getOneAsComponent(objectMapper).get());
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(objectMapper);
            }
        });
    }
}
