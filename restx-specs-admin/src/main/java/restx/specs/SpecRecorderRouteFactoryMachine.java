package restx.specs;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import restx.factory.*;

@Machine
public class SpecRecorderRouteFactoryMachine extends SingleNameFactoryMachine<SpecRecorderRoute> {
    public static final Name<SpecRecorderRoute> NAME = Name.of(SpecRecorderRoute.class, "SpecRecorderRoute");

    public SpecRecorderRouteFactoryMachine() {
        super(0, new MachineEngine<SpecRecorderRoute>() {
            private final Factory.Query<RestxSpecRecorder> specRecorder = Factory.Query.byClass(RestxSpecRecorder.class);

            @Override
            public Name getName() {
                return NAME;
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(specRecorder));
            }

            @Override
            public ComponentBox<SpecRecorderRoute> newComponent(SatisfiedBOM satisfiedBOM) {
                Optional<NamedComponent<RestxSpecRecorder>> recorder = satisfiedBOM.getOne(specRecorder);
                if (!recorder.isPresent()) {
                    return new EmptyBox<>(NAME);
                }
                return BoundlessComponentBox.FACTORY.of(new NamedComponent(NAME,
                        new SpecRecorderRoute(recorder.get().getComponent())));
            }
        });
    }

}
