package restx.specs;

import com.google.common.collect.ImmutableList;
import restx.CoreModule;
import restx.RestxContext;
import restx.common.UUIDGenerator;
import restx.factory.*;

import java.util.*;

/**
 * @author fcamblor
 */
@Component
public class GivenUUIDGeneratorRecorder implements RestxSpecRecorder.GivenRecorder {
    @Override
    public void installRecording() {
        Factory.LocalMachines.contextLocal(RestxContext.Modes.RECORDING).addMachine(
                new SingleNameFactoryMachine<>(0, new NoDepsMachineEngine<ComponentCustomizerEngine>(
                Name.of(ComponentCustomizerEngine.class, "UUIDGeneratorSequenceSupplier"),
                BoundlessComponentBox.FACTORY) {
                    @Override
                    protected ComponentCustomizerEngine doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SingleComponentClassCustomizerEngine(0, UUIDGenerator.class) {
                            @Override
                            public NamedComponent customize(final NamedComponent namedComponent) {
                                return new NamedComponent(namedComponent.getName(), new UUIDGenerator() {
                                    @Override
                                    public String doGenerate() {
                                        String uuid = ((UUIDGenerator) namedComponent.getComponent()).doGenerate();
                                        Tape.TAPE.get().recordGeneratedId(uuid);
                                        return uuid;
                                    }
                                });
                            }
                        };
                    }
                }));
    }

    @Override
    public AutoCloseable recordIn(final Map<String, Given> givens) {
        return new Tape(givens);
    }

    private static class Tape implements AutoCloseable {
        private static final ThreadLocal<Tape> TAPE = new ThreadLocal<>();
        private final Map<String, Given> givens;
        private GivenUUIDGenerator givenUUIDGenerator;

        private Tape(Map<String, Given> givens) {
            this.givens = givens;
            givenUUIDGenerator = new GivenUUIDGenerator(ImmutableList.<String>of());
            TAPE.set(this);
        }

        @Override
        public void close() throws Exception {
            TAPE.remove();
            givens.put("uuids", givenUUIDGenerator);
        }

        private void recordGeneratedId(String uuid) {
            givenUUIDGenerator = givenUUIDGenerator.concat(uuid);
        }
    }
}
