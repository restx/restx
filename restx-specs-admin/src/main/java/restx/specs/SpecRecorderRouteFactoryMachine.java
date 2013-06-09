package restx.specs;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import restx.admin.AdminPage;
import restx.factory.*;

@Machine
public class SpecRecorderRouteFactoryMachine extends DefaultFactoryMachine {
    public static final Name<SpecRecorderRoute> RECORDER_ROUTE_NAME = Name.of(SpecRecorderRoute.class, "SpecRecorderRoute");

    private static final Factory.Query<RestxSpecRecorder> specRecorder = Factory.Query.byClass(RestxSpecRecorder.class);
    private static final Name<AdminPage> ADMIN_PAGE_NAME = Name.of(AdminPage.class, "Recorder");

    public SpecRecorderRouteFactoryMachine() {
        super(0,
            // add recorder route only if spec recorder is available
            new MachineEngine<SpecRecorderRoute>() {

                @Override
                public Name getName() {
                    return RECORDER_ROUTE_NAME;
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(specRecorder));
                }

                @Override
                public ComponentBox<SpecRecorderRoute> newComponent(SatisfiedBOM satisfiedBOM) {
                    Optional<NamedComponent<RestxSpecRecorder>> recorder = satisfiedBOM.getOne(specRecorder);
                    if (!recorder.isPresent()) {
                        return new EmptyBox<>(RECORDER_ROUTE_NAME);
                    }
                    return BoundlessComponentBox.FACTORY.of(new NamedComponent(RECORDER_ROUTE_NAME,
                            new SpecRecorderRoute(recorder.get().getComponent())));
                }

                @Override
                public String toString() {
                    return "SpecRecorderRouteFactoryMachineEngine";
                }
            },
            // add admin page only if spec recorder is available
            new MachineEngine<AdminPage>() {
                @Override
                public Name<AdminPage> getName() {
                    return ADMIN_PAGE_NAME;
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(specRecorder));
                }

                @Override
                public ComponentBox<AdminPage> newComponent(SatisfiedBOM satisfiedBOM) {
                    Optional<NamedComponent<RestxSpecRecorder>> recorder = satisfiedBOM.getOne(specRecorder);
                    if (!recorder.isPresent()) {
                        return new EmptyBox<>(ADMIN_PAGE_NAME);
                    }
                    return BoundlessComponentBox.FACTORY.of(new NamedComponent(ADMIN_PAGE_NAME,
                            new AdminPage("/@/ui/recorder/", "Recorder")));
                }
            }
        );
    }

}
