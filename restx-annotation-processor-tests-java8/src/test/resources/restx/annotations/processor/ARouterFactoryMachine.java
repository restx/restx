package com.example;

import com.google.common.collect.ImmutableSet;
import restx.factory.*;
import com.example.ARouter;

@Machine
public class ARouterFactoryMachine extends SingleNameFactoryMachine<com.example.ARouter> {
    public static final Name<com.example.ARouter> NAME = Name.of(com.example.ARouter.class, "ARouter");

    public ARouterFactoryMachine() {
        super(0, new StdMachineEngine<com.example.ARouter>(NAME, 0, BoundlessComponentBox.FACTORY) {
private final Factory.Query<com.example.A> resource = Factory.Query.byClass(com.example.A.class).mandatory();
private final Factory.Query<restx.entity.EntityRequestBodyReaderRegistry> readerRegistry = Factory.Query.byClass(restx.entity.EntityRequestBodyReaderRegistry.class).mandatory();
private final Factory.Query<restx.entity.EntityResponseWriterRegistry> writerRegistry = Factory.Query.byClass(restx.entity.EntityResponseWriterRegistry.class).mandatory();
private final Factory.Query<restx.converters.MainStringConverter> converter = Factory.Query.byClass(restx.converters.MainStringConverter.class).mandatory();
private final Factory.Query<restx.security.PermissionFactory> pf = Factory.Query.byClass(restx.security.PermissionFactory.class).mandatory();
private final Factory.Query<javax.validation.Validator> validator = Factory.Query.byClass(javax.validation.Validator.class).optional();
private final Factory.Query<restx.security.RestxSecurityManager> securityManager = Factory.Query.byClass(restx.security.RestxSecurityManager.class).mandatory();
private final Factory.Query<restx.endpoint.EndpointParameterMapperRegistry> paramMapperRegistry = Factory.Query.byClass(restx.endpoint.EndpointParameterMapperRegistry.class).mandatory();

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(
resource,
readerRegistry,
writerRegistry,
converter,
pf,
validator,
securityManager,
paramMapperRegistry
                ));
            }

            @Override
            protected com.example.ARouter doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new ARouter(
satisfiedBOM.getOne(resource).get().getComponent(),
satisfiedBOM.getOne(readerRegistry).get().getComponent(),
satisfiedBOM.getOne(writerRegistry).get().getComponent(),
satisfiedBOM.getOne(converter).get().getComponent(),
satisfiedBOM.getOne(pf).get().getComponent(),
satisfiedBOM.getOneAsComponent(validator),
satisfiedBOM.getOne(securityManager).get().getComponent(),
satisfiedBOM.getOne(paramMapperRegistry).get().getComponent()
                );
            }
        });
    }

}
