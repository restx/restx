package restx.tests;

import com.google.common.collect.ImmutableMap;
import restx.common.ThreadLocalMillisProvider;
import restx.factory.AutoStartable;
import restx.factory.Component;
import restx.specs.GivenTime;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static restx.factory.Factory.LocalMachines.overrideComponents;
import static restx.factory.Factory.LocalMachines.threadLocal;

@Component
public class GivenTimeRunner implements GivenRunner<GivenTime> {
    @Override
    public Class<GivenTime> getGivenClass() {
        return GivenTime.class;
    }

    @Override
    public GivenCleaner run(GivenTime given, ImmutableMap<String, String> params) {
        threadLocal().set("FixedTimeComponent", new FixedTimeComponent(given.getTime().getMillis()));
        overrideComponents().set(Clock.class, "clock", Clock.fixed(Instant.ofEpochMilli(given.getTime().getMillis()), ZoneId.systemDefault()));
        return NoopGivenCleaner.INSTANCE;
    }

    /**
     * During tests, components are started and closed at each request.
     * So this component will set a fixed time for the duration of the request in the thread used to handle
     * the request, and clean it when closed.
     */
    private class FixedTimeComponent implements AutoStartable, AutoCloseable {
        private final long millis;

        public FixedTimeComponent(long millis) {
            this.millis = millis;
        }

        @Override
        public void start() {
            ThreadLocalMillisProvider.setCurrentMillisFixed(millis);
        }

        @Override
        public void close() throws Exception {
            ThreadLocalMillisProvider.clear();
        }
    }
}
