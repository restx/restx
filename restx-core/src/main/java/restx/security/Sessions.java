package restx.security;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Date: 17/11/13
 * Time: 16:23
 */
public class Sessions {
    public static final class SessionData implements Comparable<SessionData> {

        private final String key;
        private final long firstAccess;
        private final long lastAccess;
        private final long lastAccessNano;
        private final int count;
        private final ImmutableMap<String, String> metadata;

        private SessionData(String key, long firstAccess, long lastAccess, long lastAccessNano, int count, ImmutableMap<String, String> metadata) {
            this.key = checkNotNull(key);
            this.firstAccess = firstAccess;
            this.lastAccess = lastAccess;
            this.lastAccessNano = lastAccessNano;
            this.count = count;
            this.metadata = checkNotNull(metadata);
        }

        public String getKey() {
            return key;
        }

        public long getFirstAccess() {
            return firstAccess;
        }

        public long getLastAccess() {
            return lastAccess;
        }

        public int getCount() {
            return count;
        }

        public ImmutableMap<String, String> getMetadata() {
            return metadata;
        }

        private SessionData touch(ImmutableMap<String, String> metadata) {
            return new SessionData(key, firstAccess, System.currentTimeMillis(), System.nanoTime(), count + 1, metadata);
        }

        @Override
        public String toString() {
            return "SessionData{" +
                    "key='" + key + '\'' +
                    ", firstAccess=" + firstAccess +
                    ", lastAccess=" + lastAccess +
                    ", metadata=" + metadata +
                    '}';
        }

        @Override
        public int compareTo(SessionData o) {
            return (int) (lastAccessNano - o.lastAccessNano);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SessionData that = (SessionData) o;

            if (!key.equals(that.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

    }

    private final ConcurrentMap<String, SessionData> sessions = Maps.newConcurrentMap();
    private final int limit;

    public Sessions(int limit) {
        this.limit = limit;
    }

    public Optional<SessionData> get(String key) {
        return Optional.fromNullable(sessions.get(key));
    }

    public ImmutableMap<String, SessionData> getAll() {
        return ImmutableMap.copyOf(sessions);
    }

    public SessionData touch(String key, ImmutableMap<String, String> metadata) {
        boolean updated = false;
        SessionData updatedSessionData;
        do {
            SessionData sessionData;
            sessionData = sessions.get(key);
            if (sessionData != null) {
                updatedSessionData = sessionData.touch(metadata);
            } else {
                long access = System.currentTimeMillis();
                updatedSessionData = new SessionData(key, access, access, System.nanoTime(), 1, metadata);
            }

            updated = sessions.put(key, updatedSessionData) == sessionData;
        } while (!updated);

        // take size under limit
        // note that it may exceed the limit for a short time until the following code completes
        int size = sessions.size();
        int remainingChecks = (size - limit) * 3 + 100;
        while (sessions.size() > limit) {
            if (remainingChecks-- == 0) {
                // we have tried too many times to remove exceeding elements.
                // the possible cause is that oldest element is always updated between we find it and try to remove it
                // this is very unlikely but it's better to fail than run into an infinite loop

                throw new IllegalStateException(
                        String.format(
                                "didn't manage to limit the size of sessions data within a reasonnable (%d) number of attempts",
                                (size - limit) * 3 + 100));
            }
            SessionData oldest = Ordering.natural().leastOf(sessions.values(), 1).get(0);

            // we check if we still need to remove an element, the sessions may have changed while we were
            // looking for the oldest element
            if (sessions.size() > limit) {
                // we remove it only if it hasn't changed. If it changed the remove method of ConcurrentMap won't
                // remove it, and we will go on with the while loop
                sessions.remove(oldest.getKey(), oldest);
            }
        }

        return updatedSessionData;
    }
}
