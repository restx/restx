package restx.security;

import restx.factory.*;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 2:52 PM
*/
@Machine
public final class CORSFilterMachine extends SingleNameFactoryMachine<CORSFilter> {
    public static final Name<CORSFilter> NAME = Name.of(CORSFilter.class, "CORSFilter");

    public CORSFilterMachine() {
        super(-10, new StdMachineEngine<CORSFilter>(NAME, BoundlessComponentBox.FACTORY) {
            private final Factory.Query<CORSAuthorizer> authorizers = Factory.Query.byClass(CORSAuthorizer.class);
            @Override
            public CORSFilter doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new CORSFilter(satisfiedBOM.getAsComponents(authorizers));
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(authorizers);
            }
        });
    }
}
