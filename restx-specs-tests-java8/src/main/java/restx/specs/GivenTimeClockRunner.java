package restx.specs;

import com.google.common.collect.ImmutableMap;
import restx.factory.Component;
import restx.tests.GivenCleaner;
import restx.tests.GivenRunner;
import restx.tests.NoopGivenCleaner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static restx.factory.Factory.LocalMachines.overrideComponents;

@Component
public class GivenTimeClockRunner implements GivenRunner<GivenTime> {
    @Override
    public Class<GivenTime> getGivenClass() {
        return GivenTime.class;
    }

    @Override
    public GivenCleaner run(GivenTime given, ImmutableMap<String, String> params) {
        overrideComponents().set(Clock.class, "clock", Clock.fixed(Instant.ofEpochMilli(given.getTime().getMillis()), ZoneId.systemDefault()));
        return NoopGivenCleaner.INSTANCE;
    }

}