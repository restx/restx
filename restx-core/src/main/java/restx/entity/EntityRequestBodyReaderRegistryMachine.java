package restx.entity;

import com.google.common.collect.ImmutableSet;
import restx.factory.*;

/**
 * Date: 23/10/13
 * Time: 10:46
 */
@Machine
public class EntityRequestBodyReaderRegistryMachine extends SingleNameFactoryMachine<EntityRequestBodyReaderRegistry> {
    public static final Name<EntityRequestBodyReaderRegistry> NAME = Name.of(EntityRequestBodyReaderRegistry.class, "EntityRequestBodyReaderRegistry");

    public EntityRequestBodyReaderRegistryMachine() {
        super(1000, new StdMachineEngine<EntityRequestBodyReaderRegistry>(NAME, BoundlessComponentBox.FACTORY) {
            private final Factory.Query<EntityRequestBodyReaderFactory> factories = Factory.Query.byClass(EntityRequestBodyReaderFactory.class);
            private final Factory.Query<EntityDefaultContentTypeProvider> typeProvider = Factory.Query.byClass(EntityDefaultContentTypeProvider.class);
            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(factories, typeProvider));
            }
            @Override
            protected EntityRequestBodyReaderRegistry doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new EntityRequestBodyReaderRegistry(
                        satisfiedBOM.getAsComponents(typeProvider), satisfiedBOM.getAsComponents(factories));
            }
        });
    }
}

