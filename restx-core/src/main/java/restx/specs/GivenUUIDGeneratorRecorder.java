package restx.specs;

import restx.common.UUIDGenerator;
import restx.common.UUIDGenerators;
import restx.factory.*;

import java.util.*;

/**
 * @author fcamblor
 */
@Component
public class GivenUUIDGeneratorRecorder implements RestxSpecRecorder.GivenRecorder {
    Map<String, UUIDGenerators.OverridenMachineCleaner> namedUUIDGeneratorsCleaners = new HashMap<>();

    @Override
    public void installRecording() {
        Set<NamedComponent<UUIDGenerator>> namedGenerators = UUIDGenerators.currentUUIDGenerators();
        for(NamedComponent<UUIDGenerator> namedGenerator : namedGenerators){
            namedUUIDGeneratorsCleaners.put(namedGenerator.getName().getName(), UUIDGenerators.overrideUUIDGenerator(
                    NamedComponent.of(UUIDGenerator.class, namedGenerator.getName().getName(), new UUIDGenerator.RecordingUUIDGenerator())
            ));
        }
    }

    @Override
    public AutoCloseable recordIn(final Map<String, RestxSpec.Given> givens) {
        final Set<NamedComponent<UUIDGenerator.RecordingUUIDGenerator>> recordingUUIDGenerators = recordingUUIDGenerators();
        final Map<String, UUIDGenerator.RecordingUUIDGenerator.UUIDGeneratedObserver> observersByName = new HashMap<>();

        for(final NamedComponent<UUIDGenerator.RecordingUUIDGenerator> namedRecordingUUIDGenerator : recordingUUIDGenerators){
            UUIDGenerator.RecordingUUIDGenerator.UUIDGeneratedObserver observer = new UUIDGenerator.RecordingUUIDGenerator.UUIDGeneratedObserver() {
                public void uuidGenerated(String uuid) {
                    String key = GivenUUIDGenerator.class.getSimpleName() + "/uuidsFor" + namedRecordingUUIDGenerator.getName().getName();
                    if (!givens.containsKey(key)) {
                        givens.put(key, new GivenUUIDGenerator(namedRecordingUUIDGenerator.getName().getName(), Collections.<String>emptyList()));
                    }
                    givens.put(key, ((GivenUUIDGenerator) givens.get(key)).withAddedUUID(uuid));
                }
            };
            namedRecordingUUIDGenerator.getComponent().attachObserver(observer);
            observersByName.put(namedRecordingUUIDGenerator.getName().getName(), observer);
        }

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                for(NamedComponent<UUIDGenerator.RecordingUUIDGenerator> namedRecordingUUIDGenerator : recordingUUIDGenerators){
                    // Removing attached observers
                    namedRecordingUUIDGenerator.getComponent().detachObserver(observersByName.get(namedRecordingUUIDGenerator.getName().getName()));

                    // No need to clean uuid generators machine because installRecording() will not be called
                    // again before the next recordIn() call..
                }
            }
        };
    }

    private Set<NamedComponent<UUIDGenerator.RecordingUUIDGenerator>> recordingUUIDGenerators(){
        Set<NamedComponent<UUIDGenerator.RecordingUUIDGenerator>> recordingUUIDGenerators = new HashSet<>();
        for(Map.Entry<String,UUIDGenerators.OverridenMachineCleaner> namedUUIDGeneratorCleanerByName : namedUUIDGeneratorsCleaners.entrySet()){
            UUIDGenerator currentGenerator = UUIDGenerators.currentGeneratorFor(namedUUIDGeneratorCleanerByName.getKey());
            if(currentGenerator instanceof UUIDGenerator.RecordingUUIDGenerator){
                recordingUUIDGenerators.add(NamedComponent.of(UUIDGenerator.RecordingUUIDGenerator.class, namedUUIDGeneratorCleanerByName.getKey(), (UUIDGenerator.RecordingUUIDGenerator) currentGenerator));
            }
        }
        return recordingUUIDGenerators;
    }
}
