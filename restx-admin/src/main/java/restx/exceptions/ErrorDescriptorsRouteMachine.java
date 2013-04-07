package restx.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import restx.factory.*;
import restx.jackson.FrontObjectMapperFactory;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 2:55 PM
*/
@Machine
public class ErrorDescriptorsRouteMachine extends SingleNameFactoryMachine<ErrorDescriptorsRoute> {
    private static final Factory.Query<ErrorDescriptor> ERROR_DESCRIPTOR_QUERY =
                                            Factory.Query.byClass(ErrorDescriptor.class);
    private static final Factory.Query<ObjectMapper> OBJECT_MAPPER_QUERY =
                                            Factory.Query.byName(FrontObjectMapperFactory.NAME);

    public ErrorDescriptorsRouteMachine() {
        super(0, new StdMachineEngine<ErrorDescriptorsRoute>(
                Name.of(ErrorDescriptorsRoute.class, "ErrorDescriptorsRoute"), BoundlessComponentBox.FACTORY) {
            @Override
            protected ErrorDescriptorsRoute doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new ErrorDescriptorsRoute(satisfiedBOM.getAsComponents(ERROR_DESCRIPTOR_QUERY),
                        satisfiedBOM.getOne(OBJECT_MAPPER_QUERY).get().getComponent());
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(ERROR_DESCRIPTOR_QUERY, OBJECT_MAPPER_QUERY);
            }
        });
    }
}
