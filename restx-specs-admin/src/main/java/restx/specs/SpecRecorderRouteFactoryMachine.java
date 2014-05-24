package restx.specs;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import restx.AppSettings;
import restx.RestxContext;
import restx.admin.AdminPage;
import restx.factory.*;

@Machine
public class SpecRecorderRouteFactoryMachine extends DefaultFactoryMachine {
    public static final Name<SpecRecorderRoute> RECORDER_ROUTE_NAME = Name.of(SpecRecorderRoute.class, "SpecRecorderRoute");

    private static final Factory.Query<AppSettings> appSettings = Factory.Query.byClass(AppSettings.class).mandatory();
    private static final Factory.Query<RestxSpecRecorder.Repository> recordedSpecRepo = Factory.Query.byClass(RestxSpecRecorder.Repository.class);
    private static final Factory.Query<RestxSpec.StorageSettings> storageSettings = Factory.Query.byClass(RestxSpec.StorageSettings.class).mandatory();
    private static final Name<AdminPage> ADMIN_PAGE_NAME = Name.of(AdminPage.class, "Recorder");

    public SpecRecorderRouteFactoryMachine() {
        super(0,
            // add recorder route only if spec recorder is available
            new MachineEngine<SpecRecorderRoute>() {
                @Override
                public Name<SpecRecorderRoute> getName() {
                    return RECORDER_ROUTE_NAME;
                }

                @Override
                public int getPriority() {
                    return 0;
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(recordedSpecRepo, storageSettings));
                }

                @Override
                public ComponentBox<SpecRecorderRoute> newComponent(SatisfiedBOM satisfiedBOM) {
                    Optional<NamedComponent<RestxSpecRecorder.Repository>> recorder = satisfiedBOM.getOne(recordedSpecRepo);
                    if (!recorder.isPresent()) {
                        return new EmptyBox<>(RECORDER_ROUTE_NAME);
                    }
                    return BoundlessComponentBox.FACTORY.of(new NamedComponent<>(RECORDER_ROUTE_NAME,
                            new SpecRecorderRoute(recorder.get().getComponent(),
                                    satisfiedBOM.getOneAsComponent(storageSettings).get()
                                    )));
                }

                @Override
                public String toString() {
                    return "SpecRecorderRouteFactoryMachineEngine";
                }
            },
            // add admin page only in RECORDING mode
            new MachineEngine<AdminPage>() {
                @Override
                public Name<AdminPage> getName() {
                    return ADMIN_PAGE_NAME;
                }

                @Override
                public int getPriority() {
                    return 0;
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(appSettings));
                }

                @Override
                public ComponentBox<AdminPage> newComponent(SatisfiedBOM satisfiedBOM) {
                    AppSettings settings = satisfiedBOM.getOne(appSettings).get().getComponent();
                    if (!RestxContext.Modes.RECORDING.equals(settings.mode())) {
                        return new EmptyBox<>(ADMIN_PAGE_NAME);
                    }
                    return BoundlessComponentBox.FACTORY.of(new NamedComponent<>(ADMIN_PAGE_NAME,
                            new AdminPage("/@/ui/recorder/", "Recorder")));
                }
            }
        );
    }

}
