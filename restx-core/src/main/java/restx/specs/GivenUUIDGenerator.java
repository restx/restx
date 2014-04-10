package restx.specs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import restx.common.MoreStrings;

/**
 * @author fcamblor
 */
public class GivenUUIDGenerator implements Given {
    private final ImmutableList<String> playbackUUIDs;

    public GivenUUIDGenerator(ImmutableList<String> playbackUUIDs) {
        this.playbackUUIDs = playbackUUIDs;
    }

    public ImmutableList<String> getPlaybackUUIDs() {
        return playbackUUIDs;
    }

    public GivenUUIDGenerator concat(String uuid){
        return new GivenUUIDGenerator(ImmutableList.<String>builder().addAll(playbackUUIDs).add(uuid).build());
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(String.format("  - uuids: %s%n",
                Joiner.on(",").join(Iterables.transform(playbackUUIDs, MoreStrings.SURROUND_WITH_DOUBLE_QUOTES))));
    }
}
