package restx.jongo;

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.marshall.jackson.JacksonMapper;
import restx.factory.*;
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
        super(0, new MachineEngine<Jongo>() {
            private Factory.Query<String> dbNameQuery = Factory.Query.byName(JONGO_DB);

            @Override
            public Name<Jongo> getName() {
                return NAME;
            }

            @Override
            public BillOfMaterials getBillOfMaterial() {
                return BillOfMaterials.of(dbNameQuery);
            }

            public ComponentBox<Jongo> newComponent(SatisfiedBOM satisfiedBOM) {
                return new BoundlessComponentBox<Jongo>(
                        new NamedComponent(NAME, doNewComponent(satisfiedBOM))) {
                    @Override
                    public void close() {
                        pick().get().getComponent().getDatabase().getMongo().close();
                    }
                };
            }

            public Jongo doNewComponent(SatisfiedBOM satisfiedBOM) {
                String db = satisfiedBOM.getOne(dbNameQuery).get().getComponent();
                try {
                    return new Jongo(new MongoClient().getDB(db),
                            new JacksonMapper.Builder()
                                .registerModule(new BsonJodaTimeModule())
                                .build());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
