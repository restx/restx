package restx.common;

import com.google.common.base.Optional;
import restx.factory.*;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.factory.Factory.LocalMachines.contextLocal;

/**
 * @author fcamblor
 */
public class UUIDGenerators {

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

    public static UUIDGenerator currentGeneratorFor(Name<UUIDGenerator> name){
        Optional<NamedComponent<UUIDGenerator>> namedComponent = defaultFactory().queryByName(name).findOne();
        if(namedComponent.isPresent()){
            return namedComponent.get().getComponent();
        } else {
            return null;
        }
    }

    public static OverridenMachineCleaner overrideUUIDGenerator(final NamedComponent<UUIDGenerator> replacementGenerator) {
        FactoryMachine temporaryAddedMachine = new SingletonFactoryMachine(-10000, replacementGenerator);
        OverridenMachineCleaner cleaner = new OverridenMachineCleaner(temporaryAddedMachine, UUID_GENERATORS_LOCAL_MACHINES_CONTEXT);
        contextLocal(checkNotNull(UUID_GENERATORS_LOCAL_MACHINES_CONTEXT, "contextName param is required")).addMachine(temporaryAddedMachine);
        return cleaner;
    }

    public static void provideAnotherUUIDGeneratorDuring(Runnable runnable, final NamedComponent<UUIDGenerator> replacementGenerator) {
        OverridenMachineCleaner cleaner = overrideUUIDGenerator(replacementGenerator);
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

