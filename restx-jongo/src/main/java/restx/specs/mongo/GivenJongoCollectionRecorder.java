package restx.specs.mongo;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.ObjectIdUpdater;
import org.jongo.ResultHandler;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.QueryFactory;
import restx.RestxContext;
import restx.factory.*;
import restx.jongo.JongoCollection;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecRecorder;

import java.util.Map;

/**
* User: xavierhanin
* Date: 3/30/13
* Time: 7:02 PM
*/
@Component
public class GivenJongoCollectionRecorder implements RestxSpecRecorder.GivenRecorder {
    @Override
    public void installRecording() {
        final Factory.Query<Mapper> mapperQuery = Factory.Query.byClass(Mapper.class);
        Factory.LocalMachines.contextLocal(RestxContext.Modes.RECORDING).addMachine(
                new SingleNameFactoryMachine<>(0, new StdMachineEngine<ComponentCustomizerEngine>(
                                    Name.of(ComponentCustomizerEngine.class, "JongoCollectionSequenceSupplier"),
                                    BoundlessComponentBox.FACTORY) {
                    @Override
                    protected ComponentCustomizerEngine doNewComponent(SatisfiedBOM satisfiedBOM) {
                        final Mapper mapper = satisfiedBOM.getOne(mapperQuery).get().getComponent();
                        final ObjectIdUpdater objectIdUpdater = mapper.getObjectIdUpdater();
                        return new SingleComponentClassCustomizerEngine<JongoCollection>(0, JongoCollection.class) {
                            @Override
                            public NamedComponent<JongoCollection> customize(NamedComponent<JongoCollection> namedComponent) {
                                final JongoCollection collection = namedComponent.getComponent();
                                return new NamedComponent<>(namedComponent.getName(),
                                        new SequencingJongoCollection(collection, mapper, objectIdUpdater));
                            }
                        };
                    }

                    @Override
                    public BillOfMaterials getBillOfMaterial() {
                        return BillOfMaterials.of(mapperQuery);
                    }
                }));
    }

    @Override
    public AutoCloseable recordIn(Map<String, RestxSpec.Given> givens) {
        return new Tape(givens);
    }

    private static class Tape implements AutoCloseable {
        private static final ThreadLocal<Tape> TAPE = new ThreadLocal<>();

        private final Map<String, RestxSpec.Given> givens;

        private Tape(Map<String, RestxSpec.Given> givens) {
            this.givens = givens;
            TAPE.set(this);
        }

        @Override
        public void close() throws Exception {
            TAPE.remove();
        }

        public void recordCollection(MongoCollection mongoCollection) {
            String key = getGivenCollectionKey(mongoCollection.getName());
            if (givens.containsKey(key)) {
                return;
            }
            Stopwatch stopwatch = new Stopwatch().start();
            System.out.print("RECORDING " + mongoCollection.getName() + "...");
            Iterable<String> items = mongoCollection.find().map(new ResultHandler<String>() {
                @Override
                public String map(DBObject result) {
                    return JSON.serialize(result);
                }
            });

            givens.put(key, new GivenJongoCollection(mongoCollection.getName(), "", "       " + Joiner.on("\n       ").join(items), ImmutableList.<String>of()));
            System.out.println(" >> recorded " + mongoCollection.getName() + " -- " + stopwatch.toString());
        }

        private String getGivenCollectionKey(String name) {
            return name;
        }

        private void recordGeneratedId(String name, ObjectId id) {
            String key = getGivenCollectionKey(name);
            RestxSpec.Given given = givens.get(key);
            if (given instanceof GivenJongoCollection) {
                GivenJongoCollection collection = (GivenJongoCollection) given;
                givens.put(key, collection.addSequenceId(id.toString()));
                System.out.println(" >> recorded OID " + name + " > " + id);
            }
        }
    }

    private static class SequencingJongoCollection implements JongoCollection {
        private final JongoCollection collection;
        private final Mapper mapper;
        private final ObjectIdUpdater objectIdUpdater;

        public SequencingJongoCollection(JongoCollection collection, Mapper mapper, ObjectIdUpdater objectIdUpdater) {
            this.collection = collection;
            this.mapper = mapper;
            this.objectIdUpdater = objectIdUpdater;
        }

        @Override
        public String getName() {
            return collection.getName();
        }

        @Override
        public MongoCollection get() {
            MongoCollection mongoCollection = collection.get();
            if (Tape.TAPE.get() != null) {
                Tape.TAPE.get().recordCollection(mongoCollection);
                mongoCollection = new MongoCollection(
                        mongoCollection.getDBCollection(),
                        new Mapper() {
                            @Override
                            public Marshaller getMarshaller() {
                                return mapper.getMarshaller();
                            }

                            @Override
                            public Unmarshaller getUnmarshaller() {
                                return mapper.getUnmarshaller();
                            }

                            @Override
                            public ObjectIdUpdater getObjectIdUpdater() {
                                return new ObjectIdUpdater() {
                                    @Override
                                    public boolean isNew(Object target) {
                                        return objectIdUpdater.isNew(target);
                                    }

                                    @Override
                                    public void setObjectId(Object target, ObjectId id) {
                                        Tape.TAPE.get().recordGeneratedId(collection.getName(), id);
                                        objectIdUpdater.setObjectId(target, id);
                                    }
                                };
                            }

                            @Override
                            public QueryFactory getQueryFactory() {
                                return mapper.getQueryFactory();
                            }
                        }
                );
            }
            return mongoCollection;
        }
    }
}
