package restx.jongo;

import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;
import restx.factory.Module;
import restx.factory.Provides;
import restx.jackson.BsonJodaTimeModule;
import restx.jackson.Views;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
@Module
public class JongoModule {

    @Provides
    public Mapper mapper() {
        return new JacksonMapper.Builder()
                        .registerModule(new BsonJodaTimeModule())
                        .withView(Views.Private.class)
                        .build();
    }

}
