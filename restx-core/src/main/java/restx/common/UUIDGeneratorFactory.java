package restx.common;

import com.google.common.base.Optional;
import restx.factory.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.factory.Factory.LocalMachines.contextLocal;

/**
 * @author fcamblor
 */
@Machine
public class UUIDGeneratorFactory implements FactoryMachine {

    private static final Factory.Query<UUIDGenerator> UUID_GENERATOR_QUERY = Factory.Query.byClass(UUIDGenerator.class);
    private static final String UUID_GENERATORS_LOCAL_MACHINES_CONTEXT = "uuidGeneratorsLocalMachinesContext";

    public static class OverridenMachineCleaner {
        FactoryMachine machineToClean;
        String contextName;

        public OverridenMachineCleaner(FactoryMachine machineToClean, String contextName) {
            this.machineToClean = machineToClean;
            this.contextName = checkNotNull(contextName, "contextName param is required");
        }

        public void cleanup(){
            contextLocal(contextName).removeMachine(machineToClean);
        }
    }

    @Override
    public boolean canBuild(Name<?> name) {
        return UUIDGenerator.class.isAssignableFrom(name.getClazz());
    }

    @Override
    public <T> MachineEngine<T> getEngine(final Name<T> name) {
        return new StdMachineEngine<T>(name, BoundlessComponentBox.FACTORY) {
            @Override
            protected T doNewComponent(SatisfiedBOM satisfiedBOM) {
                // This machine engine could contain the same UUIDGenerator named component (with same name) more than once
                // Particularly when we call the overwriteUUIDGenerator()
                // In that case, we return the first component matching both class & name, that is to say, component
                // having the lowest priority
                for(NamedComponent<UUIDGenerator> uuidGenerator : satisfiedBOM.get(UUID_GENERATOR_QUERY)){
                    if(uuidGenerator.getName().getName().equals(name.getName())) {
                        return (T) uuidGenerator.getComponent();
                    }
                }
                return null;
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(UUID_GENERATOR_QUERY);
            }
        };
    }

    @Override
    public <T> Set<Name<T>> nameBuildableComponents(Class<T> componentClass) {
        return Collections.emptySet();
    }

    @Override
    public int priority() {
        return 1000;
    }

    @Override
    public String toString() {
        return "UUIDGeneratorFactory{" +
                "uuidGeneratorsQuery=" + UUID_GENERATOR_QUERY +
                '}';
    }

    public static UUIDGenerator currentGeneratorFor(Name<UUIDGenerator> name){
        Optional<NamedComponent<UUIDGenerator>> namedComponent = defaultFactory().queryByName(name).findOne();
        if(namedComponent.isPresent()){
            return namedComponent.get().getComponent();
        } else {
            return null;
        }
    }

    public static OverridenMachineCleaner overwriteUUIDGenerator(final NamedComponent<UUIDGenerator> replacementGenerator) {
        FactoryMachine temporaryAddedMachine = new SingletonFactoryMachine(-10, replacementGenerator);
        OverridenMachineCleaner cleaner = new OverridenMachineCleaner(temporaryAddedMachine, UUID_GENERATORS_LOCAL_MACHINES_CONTEXT);
        contextLocal(checkNotNull(UUID_GENERATORS_LOCAL_MACHINES_CONTEXT, "contextName param is required")).addMachine(temporaryAddedMachine);
        return cleaner;
    }

    public static void provideAnotherUUIDGeneratorDuring(Runnable runnable, final NamedComponent<UUIDGenerator> replacementGenerator) {
        OverridenMachineCleaner cleaner = overwriteUUIDGenerator(replacementGenerator);
        try {
            runnable.run();
        } finally {
            cleaner.cleanup();
        }
    }

    public static void playback(List<String> sequence, Runnable runnable, Name<UUIDGenerator> name) {
        NamedComponent<UUIDGenerator> namedComponent = NamedComponent.of(
                UUIDGenerator.class, name.getName(),
                new UUIDGenerator.PlaybackUUIDGenerator(sequence));
        provideAnotherUUIDGeneratorDuring(runnable, namedComponent);
    }

    public static void record(Runnable runnable, Name<UUIDGenerator> name) {
        NamedComponent<UUIDGenerator> namedComponent = NamedComponent.of(
                UUIDGenerator.class, name.getName(),
                new UUIDGenerator.RecordingUUIDGenerator());
        provideAnotherUUIDGeneratorDuring(runnable, namedComponent);
    }

    private static Factory defaultFactory(){
        return Factory.builder()
                .addLocalMachines(Factory.LocalMachines.threadLocal())
                // TODO : Would be better to have the serverId (context_name) given
                // as a http request header, in order to maintain sort of "session affinity" between
                // client and server, especially during test cases
                .addLocalMachines(Factory.LocalMachines.contextLocal(UUID_GENERATORS_LOCAL_MACHINES_CONTEXT))
                .addFromServiceLoader()
                .build();
    }
}

