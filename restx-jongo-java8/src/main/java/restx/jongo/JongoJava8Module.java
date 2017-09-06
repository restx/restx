package restx.jongo;

import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;
import restx.factory.Module;
import restx.factory.Provides;
import restx.jackson.BsonJodaTimeModule;
import restx.jackson.Views;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
@Module(priority = -10)
public class JongoJava8Module {
    @Provides @Named("Mapper")
    public Mapper mapper() {
        return new JacksonMapper.Builder()
                        .registerModule(new BsonJodaTimeModule()) // to keep compatibility with joda time
                        .registerModule(new BsonJSR310Module())
                        .withView(Views.Private.class)
                        .build();
    }
}
