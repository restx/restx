package restx.specs;

import com.google.common.collect.ImmutableList;
import restx.common.UUIDGenerator;
import restx.factory.BoundlessComponentBox;
import restx.factory.Component;
import restx.factory.ComponentCustomizerEngine;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.NamedComponent;
import restx.factory.NoDepsMachineEngine;
import restx.factory.SatisfiedBOM;
import restx.factory.SingleComponentClassCustomizerEngine;
import restx.factory.SingleNameFactoryMachine;

import java.util.Map;

/**
 * @author fcamblor
 */
@Component
public class GivenUUIDGeneratorRecorder implements RestxSpecRecorder.GivenRecorder {
    @Override
    public AutoCloseable recordIn(final Map<String, Given> givens) {
        final Tape tape = new Tape(givens);
        Factory.LocalMachines.threadLocal().addMachine(
                new SingleNameFactoryMachine<>(0, new NoDepsMachineEngine<ComponentCustomizerEngine>(
                        Name.of(ComponentCustomizerEngine.class, "UUIDGeneratorSequenceSupplier"),
                        BoundlessComponentBox.FACTORY) {
                    @Override
                    protected ComponentCustomizerEngine doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SingleComponentClassCustomizerEngine<UUIDGenerator>(0, UUIDGenerator.class) {
                            @Override
                            public NamedComponent<UUIDGenerator> customize(final NamedComponent<UUIDGenerator> namedComponent) {
                                return new NamedComponent<>(namedComponent.getName(), new UUIDGenerator() {
                                    @Override
                                    public String doGenerate() {
                                        String uuid = namedComponent.getComponent().doGenerate();
                                        tape.recordGeneratedId(uuid);
                                        return uuid;
                                    }
                                });
                            }
                        };
                    }
                }));
        return tape;
    }

    private static class Tape implements AutoCloseable {
        private final Map<String, Given> givens;
        private GivenUUIDGenerator givenUUIDGenerator;

        private Tape(Map<String, Given> givens) {
            this.givens = givens;
            givenUUIDGenerator = new GivenUUIDGenerator(ImmutableList.<String>of());
        }

        @Override
        public void close() throws Exception {
            if (!givenUUIDGenerator.getPlaybackUUIDs().isEmpty()) {
                givens.put("uuids", givenUUIDGenerator);
            }
        }

        private void recordGeneratedId(String uuid) {
            givenUUIDGenerator = givenUUIDGenerator.concat(uuid);
        }
    }
}
