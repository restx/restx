package restx.specs;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * @author fcamblor
 */
public class GivenUUIDGenerator implements RestxSpec.Given {
    private final String targetComponentName;
    private final ImmutableList<String> playbackUUIDs;

    public GivenUUIDGenerator(String targetComponentName, List<String> playbackUUIDs) {
        this.targetComponentName = targetComponentName;
        this.playbackUUIDs = ImmutableList.copyOf(playbackUUIDs);
    }

    public String getTargetComponentName() {
        return targetComponentName;
    }

    public ImmutableList<String> getPlaybackUUIDs() {
        return playbackUUIDs;
    }

    public GivenUUIDGenerator withAddedUUID(String uuid){
        return new GivenUUIDGenerator(targetComponentName, ImmutableList.<String>builder().addAll(playbackUUIDs).add(uuid).build());
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(String.format("  - uuidsFor: %s%n    data: %s%n", targetComponentName, playbackUUIDs));
    }
}
