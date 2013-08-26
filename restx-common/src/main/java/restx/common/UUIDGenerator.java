package restx.common;

import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 3:06 PM
 */
public abstract class UUIDGenerator {

    public static final UUIDGenerator DEFAULT = new DefaultUUIDGenerator();

    public static RecordingUUIDGenerator record() {
        RecordingUUIDGenerator recordingUUIDGenerator = new RecordingUUIDGenerator();
        return recordingUUIDGenerator;
    }

    public abstract String doGenerate();

    private static class DefaultUUIDGenerator extends UUIDGenerator {
        @Override
        public String doGenerate() {
            return UUID.randomUUID().toString();
        }
    }

    public static class RecordingUUIDGenerator extends UUIDGenerator {
        private final List<String> sequence = Lists.newArrayList();

        @Override
        public String doGenerate() {
            String uuid = DEFAULT.doGenerate();
            sequence.add(uuid);
            return uuid;
        }
    }
    public static class PlaybackUUIDGenerator extends UUIDGenerator {
        private final List<String> sequence;

        public PlaybackUUIDGenerator(List<String> sequence) {
            this.sequence = new LinkedList<>(sequence);
        }

        @Override
        public synchronized String doGenerate() {
            return sequence.remove(0);
        }
    }
}
