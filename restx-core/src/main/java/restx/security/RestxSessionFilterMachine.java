package restx.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import restx.SignatureKey;
import restx.factory.*;
import restx.jackson.FrontObjectMapperFactory;

/**
* User: xavierhanin
* Date: 3/31/13
* Time: 2:51 PM
*/
@Machine
public class RestxSessionFilterMachine extends SingleNameFactoryMachine<RestxSessionFilter> {

    public RestxSessionFilterMachine() {
        super(-10, new StdMachineEngine<RestxSessionFilter>(RestxSessionFilter.NAME, BoundlessComponentBox.FACTORY) {
            private final Factory.Query<RestxSession.Definition.Entry> entries = Factory.Query.byClass(RestxSession.Definition.Entry.class);
            private final Factory.Query<ObjectMapper> mapper = Factory.Query.byName(FrontObjectMapperFactory.NAME);
            private final Factory.Query<SignatureKey> signatureKeyQuery = Factory.Query.byClass(SignatureKey.class);
            @Override
            public RestxSessionFilter doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new RestxSessionFilter(
                        new RestxSession.Definition(satisfiedBOM.getAsComponents(entries)),
                        satisfiedBOM.getOne(mapper).get().getComponent(),
                        satisfiedBOM.getOne(signatureKeyQuery)
                                .or(new NamedComponent(
                                        Name.of(SignatureKey.class, "DefaultSignature"),
                                        new SignatureKey("this is the default signature key".getBytes())))
                                .getComponent().getKey());
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(entries, mapper, signatureKeyQuery);
            }
        });
    }
}
