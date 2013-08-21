package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.common.UUIDGenerator;
import restx.common.UUIDGenerators;
import restx.factory.Component;
import restx.factory.NamedComponent;
import restx.specs.GivenUUIDGenerator;

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
                UUIDGenerator.class, given.getTargetComponentName(),
                new UUIDGenerator.PlaybackUUIDGenerator(given.getPlaybackUUIDs()));

        final UUIDGenerators.OverridenMachineCleaner cleaner = UUIDGenerators.overrideUUIDGenerator(playbackUUIDComponent);

        return new GivenCleaner() {
            @Override
            public void cleanUp() {
                cleaner.cleanup();
            }
        };
    }
}
