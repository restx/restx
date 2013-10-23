package restx.entity;

import com.google.common.collect.ImmutableSet;
import restx.factory.*;

/**
 * Date: 23/10/13
 * Time: 10:46
 */
@Machine
public class EntityResponseWriterRegistryMachine extends SingleNameFactoryMachine<EntityResponseWriterRegistry> {
    public static final Name<EntityResponseWriterRegistry> NAME = Name.of(EntityResponseWriterRegistry.class, "EntityResponseWriterRegistry");

    public EntityResponseWriterRegistryMachine() {
        super(1000, new StdMachineEngine<EntityResponseWriterRegistry>(NAME, BoundlessComponentBox.FACTORY) {
            private final Factory.Query<EntityResponseWriterFactory> factories = Factory.Query.byClass(EntityResponseWriterFactory.class);
            private final Factory.Query<EntityDefaultContentTypeProvider> typeProvider = Factory.Query.byClass(EntityDefaultContentTypeProvider.class);
            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(factories, typeProvider));
            }
            @Override
            protected EntityResponseWriterRegistry doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new EntityResponseWriterRegistry(satisfiedBOM.getAsComponents(typeProvider), satisfiedBOM.getAsComponents(factories));
            }
        });
    }
}

