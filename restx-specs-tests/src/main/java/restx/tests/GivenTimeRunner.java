package restx.tests;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTimeUtils;
import restx.factory.Component;
import restx.specs.GivenTime;

@Component
public class GivenTimeRunner implements GivenRunner<GivenTime> {
    @Override
    public Class<GivenTime> getGivenClass() {
        return GivenTime.class;
    }

    @Override
    public GivenCleaner run(GivenTime given, ImmutableMap<String, String> params) {
        DateTimeUtils.setCurrentMillisFixed(given.getTime().getMillis());

        return new GivenCleaner() {
            @Override
            public void cleanUp() {
                DateTimeUtils.setCurrentMillisSystem();
            }
        };
    }
}
