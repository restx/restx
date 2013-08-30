package restx.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 3:06 PM
 */
public abstract class UUIDGenerator {

    public static final UUIDGenerator DEFAULT = new DefaultUUIDGenerator();

    public abstract String doGenerate();

    private static class DefaultUUIDGenerator extends UUIDGenerator {
        @Override
        public String doGenerate() {
            return UUID.randomUUID().toString();
        }
    }

    public static class RecordingUUIDGenerator extends UUIDGenerator {
        public interface UUIDGeneratedObserver {
            public void uuidGenerated(String uuid);
        }

        private List<UUIDGeneratedObserver> observers = newArrayList();

        @Override
        public String doGenerate() {
            String uuid = DEFAULT.doGenerate();
            for(UUIDGeneratedObserver uuidGeneratedObserver : observers){
                uuidGeneratedObserver.uuidGenerated(uuid);
            }
            return uuid;
        }

        public void attachObserver(UUIDGeneratedObserver uuidGeneratedObserver) {
            observers.add(uuidGeneratedObserver);
        }

        public void detachObserver(UUIDGeneratedObserver uuidGeneratedObserver) {
            observers.remove(uuidGeneratedObserver);
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
