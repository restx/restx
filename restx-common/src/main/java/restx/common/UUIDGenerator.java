package restx.common;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 3:06 PM
 */
public interface UUIDGenerator {
    String doGenerate();

    public static class DefaultUUIDGenerator implements UUIDGenerator {
        @Override
        public String doGenerate() {
            return UUID.randomUUID().toString();
        }
    }

    public static class PlaybackUUIDGenerator implements UUIDGenerator {
        public static UUIDGenerator playbackUUIDs(String... uuids) {
            return new PlaybackUUIDGenerator(newArrayList(uuids).iterator());
        }
        public static UUIDGenerator playbackUUIDs(Iterable<String> uuids) {
            return new PlaybackUUIDGenerator(uuids.iterator());
        }

        private final Iterator<String> sequence;

        private PlaybackUUIDGenerator(Iterator<String> sequence) {
            this.sequence = sequence;
        }

        @Override
        public String doGenerate() {
            if (sequence.hasNext()) {
                return sequence.next();
            } else {
                throw new IllegalStateException("no more uuids in sequence");
            }
        }
    }
}
