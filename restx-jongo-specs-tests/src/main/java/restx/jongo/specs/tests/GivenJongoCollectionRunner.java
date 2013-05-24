package restx.jongo.specs.tests;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.ObjectIdUpdater;
import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.QueryFactory;
import restx.factory.*;
import restx.jongo.JongoCollection;
import restx.specs.mongo.GivenJongoCollection;
import restx.tests.GivenCleaner;
import restx.tests.GivenRunner;

import java.net.UnknownHostException;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.factory.Factory.LocalMachines.contextLocal;
import static restx.specs.RestxSpec.WhenHttpRequest.CONTEXT_NAME;
import static restx.specs.mongo.GivenJongoCollection.DB_URI;

@Component
public class GivenJongoCollectionRunner implements GivenRunner<GivenJongoCollection> {
    @Override
    public Class<GivenJongoCollection> getGivenClass() {
        return GivenJongoCollection.class;
    }

    public GivenCleaner run(final GivenJongoCollection given, final ImmutableMap<String, String> params) {
        try {
            MongoClientURI mongoClientURI = new MongoClientURI(
                    checkNotNull(params.get(DB_URI),
                            DB_URI + " param is required"));
            Jongo jongo = new Jongo(new MongoClient(mongoClientURI).getDB(mongoClientURI.getDatabase()));
            Stopwatch stopwatch = new Stopwatch().start();
            MongoCollection collection = jongo.getCollection(given.getCollection());
            Iterable<String> items = Splitter.on("\n").trimResults().omitEmptyStrings().split(given.getData());
            int count = 0;
            for (String item : items) {
                collection.insert(item);
                count++;
            }
            System.out.printf("imported %s[%d] -- %s%n", given.getCollection(), count, stopwatch.stop().toString());

            final UnmodifiableIterator<String> it = given.getSequence().iterator();
            final CollectionSequence iteratingSequence = new CollectionSequence() {
                @Override
                public Optional<String> next() {
                    return it.hasNext() ? Optional.of(it.next()) : Optional.<String>absent();
                }
            };

            final Factory.Query<Mapper> mapperQuery = Factory.Query.byClass(Mapper.class);
            contextLocal(checkNotNull(params.get(CONTEXT_NAME), CONTEXT_NAME + " param is required")).addMachine(
                    new SingleNameFactoryMachine<>(0, new StdMachineEngine<ComponentCustomizerEngine>(
                            Name.of(ComponentCustomizerEngine.class, "JongoCollectionSequenceSupplier"),
                            BoundlessComponentBox.FACTORY) {
                        @Override
                        public BillOfMaterials getBillOfMaterial() {
                            return BillOfMaterials.of(mapperQuery);
                        }

                        @Override
                        protected ComponentCustomizerEngine doNewComponent(final SatisfiedBOM satisfiedBOM) {
                            return new SingleComponentNameCustomizerEngine<JongoCollection>(0, Name.of(JongoCollection.class, given.getCollection())) {
                                @Override
                                public NamedComponent<JongoCollection> customize(NamedComponent<JongoCollection> namedComponent) {
                                    if (namedComponent.getName().equals(given.getCollection())) {
                                        Mapper mapper = satisfiedBOM.getOne(mapperQuery).get().getComponent();
                                        return new NamedComponent<>(namedComponent.getName(),
                                                new SequencedJongoCollection(namedComponent.getComponent(), mapper,
                                                        mapper.getObjectIdUpdater(), iteratingSequence));
                                    } else {
                                        return namedComponent;
                                    }

                                }
                            };
                        }
                    }));

            return new GivenCleaner() {
                @Override
                public void cleanUp() {
                    try {
                        MongoClientURI mongoClientURI = new MongoClientURI(
                                checkNotNull(params.get(DB_URI),
                                        DB_URI + " param is required"));
                        Jongo jongo = new Jongo(new MongoClient(mongoClientURI).getDB(mongoClientURI.getDatabase()));
                        Stopwatch stopwatch = new Stopwatch().start();
                        jongo.getCollection(given.getCollection()).drop();
                        System.out.printf("dropped %s -- %s%n", given.getCollection(), stopwatch.stop().toString());
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static interface CollectionSequence {
        Optional<String> next();
    }

    private class SequencedJongoCollection implements JongoCollection {
        private final JongoCollection collection;
        private final Mapper mapper;
        private final ObjectIdUpdater objectIdUpdater;
        private final CollectionSequence iteratingSequence;

        public SequencedJongoCollection(JongoCollection collection, Mapper mapper,
                                        ObjectIdUpdater objectIdUpdater, CollectionSequence iteratingSequence) {
            this.collection = collection;
            this.mapper = mapper;
            this.objectIdUpdater = objectIdUpdater;
            this.iteratingSequence = iteratingSequence;
        }

        @Override
        public String getName() {
            return collection.getName();
        }

        @Override
        public MongoCollection get() {
            MongoCollection mongoCollection = new MongoCollection(
                    collection.get().getDBCollection(),
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
                                    objectIdUpdater.setObjectId(target,
                                            new ObjectId(iteratingSequence.next().or(id.toString())));
                                }
                            };
                        }

                        @Override
                        public QueryFactory getQueryFactory() {
                            return mapper.getQueryFactory();
                        }
                    });
            return mongoCollection;
        }
    }

}
