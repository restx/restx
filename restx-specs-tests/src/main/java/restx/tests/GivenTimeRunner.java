package restx.tests;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTimeUtils;
import restx.factory.Component;
import restx.specs.RestxSpec;

@Component
public class GivenTimeRunner implements GivenRunner<RestxSpec.GivenTime> {
    @Override
    public Class<RestxSpec.GivenTime> getGivenClass() {
        return RestxSpec.GivenTime.class;
    }

    @Override
    public GivenCleaner run(RestxSpec.GivenTime given, ImmutableMap<String, String> params) {
        DateTimeUtils.setCurrentMillisFixed(given.getTime().getMillis());

        return new GivenCleaner() {
            @Override
            public void cleanUp() {
                DateTimeUtils.setCurrentMillisSystem();
            }
        };
    }
}
