package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.CoreModule;
import restx.common.UUIDGenerator;
import restx.factory.Component;
import restx.factory.NamedComponent;
import restx.specs.GivenUUIDGenerator;

import static restx.common.UUIDGenerator.PlaybackUUIDGenerator.playbackUUIDs;
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
        NamedComponent<UUIDGenerator> playbackUUIDComponent = NamedComponent.of(
                UUIDGenerator.class, CoreModule.UUID_GENERATOR,
                playbackUUIDs(given.getPlaybackUUIDs()));
        overrideComponents().set(playbackUUIDComponent);

        return NoopGivenCleaner.INSTANCE;
    }
}
