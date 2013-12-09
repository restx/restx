package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.CoreModule;
import restx.common.UUIDGenerator;
import restx.factory.Component;
import restx.factory.NamedComponent;
import restx.specs.GivenUUIDGenerator;

import java.util.LinkedList;
import java.util.List;

import static restx.factory.Factory.LocalMachines.overrideComponents;

/**
 * @author fcamblor
 */
@Component
public class GivenUUIDGeneratorRunner implements GivenRunner<GivenUUIDGenerator> {
    @Override
    public Class<GivenUUIDGenerator> getGivenClass() {
        return GivenUUIDGenerator.class;
    }

    @Override
    public GivenCleaner run(GivenUUIDGenerator given, ImmutableMap<String, String> params) {
        final List<String> sequence1 = given.getPlaybackUUIDs();
        NamedComponent<UUIDGenerator> playbackUUIDComponent = NamedComponent.of(
                UUIDGenerator.class, CoreModule.UUID_GENERATOR,
                new UUIDGenerator() {
                    private final List<String> sequence = new LinkedList<>(sequence1);

                    @Override
                    public synchronized String doGenerate() {
                        return sequence.remove(0);
                    }
                });
        overrideComponents().set(playbackUUIDComponent);

        return NoopGivenCleaner.INSTANCE;
    }
}
