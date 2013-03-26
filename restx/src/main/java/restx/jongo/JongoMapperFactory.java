package restx.jongo;

import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;
import restx.factory.*;
import restx.jackson.BsonJodaTimeModule;
import restx.jackson.Views;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
public class JongoMapperFactory extends SingleNameFactoryMachine<Mapper> {
    public static final Name<Mapper> NAME = Name.of(Mapper.class, "JongoMapper");

    public JongoMapperFactory() {
        super(0, new NoDepsMachineEngine<Mapper>(NAME, BoundlessComponentBox.FACTORY) {

            @Override
            protected Mapper doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new JacksonMapper.Builder()
                                .registerModule(new BsonJodaTimeModule())
                                .withView(Views.Private.class)
                                .build();
            }
        });
    }
}
