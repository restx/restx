package restx.jongo;

import com.mongodb.Mongo;
import org.jongo.Jongo;
import org.jongo.marshall.jackson.JacksonMapper;
import restx.factory.BoundlessComponentBox;
import restx.factory.Factory;
import restx.factory.Name;
import restx.factory.SingleNameFactoryMachine;
import restx.jackson.BsonJodaTimeModule;

import java.net.UnknownHostException;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 12:12 AM
 */
public class JongoFactory extends SingleNameFactoryMachine<Jongo> {
    public static final String JONGO_DB_NAME = "mongo.db";
    public static final Name<String> JONGO_DB = Name.of(String.class, JONGO_DB_NAME);
    public static final Name<Jongo> NAME = Name.of(Jongo.class, "Jongo");

    public JongoFactory() {
        super(0, NAME, BoundlessComponentBox.FACTORY);
    }

    @Override
    protected Jongo doNewComponent(Factory factory) {
        String db = factory.getNamedComponent(JONGO_DB).get().getComponent();
        try {
            return new Jongo(new Mongo().getDB(db),
                    new JacksonMapper.Builder()
                        .registerModule(new BsonJodaTimeModule())
                        .build());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
