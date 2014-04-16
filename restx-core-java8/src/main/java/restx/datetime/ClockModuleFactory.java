package restx.datetime;

import restx.factory.Module;
import restx.factory.Provides;

import java.time.Clock;

@Module(priority = 1000)
public class ClockModuleFactory {

     @Provides
     public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
