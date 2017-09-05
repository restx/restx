package restx.http;

import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

@Module(priority = 100)
public class HttpModule {
    @Provides @Named("CurrentLocaleResolver")
    public CurrentLocaleResolver currentLocaleResolver(){
        return new RequestBasedLocaleResolver();
    }
}
