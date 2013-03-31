package restx.exceptions;

import restx.factory.*;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 2:55 PM
*/
@Machine
public class ErrorDescriptorsRouteMachine extends SingleNameFactoryMachine<ErrorDescriptorsRoute> {
    private static final restx.factory.Factory.Query<ErrorDescriptor> ERROR_DESCRIPTOR_QUERY =
                                            restx.factory.Factory.Query.byClass(ErrorDescriptor.class);

    public ErrorDescriptorsRouteMachine() {
        super(0, new StdMachineEngine<ErrorDescriptorsRoute>(
                Name.of(ErrorDescriptorsRoute.class, "SpecRecorderRoute"), BoundlessComponentBox.FACTORY) {
            @Override
            protected ErrorDescriptorsRoute doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new ErrorDescriptorsRoute(satisfiedBOM.getAsComponents(ERROR_DESCRIPTOR_QUERY));
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(ERROR_DESCRIPTOR_QUERY);
            }
        });
    }
}
