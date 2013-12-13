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
    public AutoCloseable recordIn(final Map<String, Given> givens) {
        final Tape tape = new Tape(givens);
        Factory.LocalMachines.threadLocal().addMachine(
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
            givens.put("uuids", givenUUIDGenerator);
        }

        private void recordGeneratedId(String uuid) {
            givenUUIDGenerator = givenUUIDGenerator.concat(uuid);
        }
    }
}
