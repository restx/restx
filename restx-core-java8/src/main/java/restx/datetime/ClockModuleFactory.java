package restx.datetime;

import restx.factory.Module;
import restx.factory.Provides;

import java.time.Clock;

 @Module
public class ClockModuleFactory {

     @Provides
     public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
