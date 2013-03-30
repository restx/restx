package restx.specs.mongo;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
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
import restx.factory.Factory;
import restx.factory.FactoryMachineWrapper;
import restx.factory.NamedComponent;
import restx.factory.SatisfiedBOM;
import restx.jongo.JongoCollection;
import restx.jongo.StdJongoCollection;
import restx.specs.RestxSpec;

import java.net.UnknownHostException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MoreStrings.reindent;
import static restx.factory.Factory.LocalMachines.contextLocal;
import static restx.specs.RestxSpec.WhenHttpRequest.CONTEXT_NAME;

/**
* User: xavierhanin
* Date: 3/30/13
* Time: 5:54 PM
*/
public class GivenJongoCollection implements RestxSpec.Given {
    public static final String DB_URI = "GivenCollection.DB_URI";

    private final String collection;
    private final String path;
    private final String data;
    private final ImmutableList<String> sequence;

    public GivenJongoCollection(String collection, String path, String data, ImmutableList<String> sequence) {
        this.collection = collection;
        this.path = path;
        this.data = data;
        this.sequence = sequence;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append("  - collection: ").append(collection).append("\n");
        if (!Strings.isNullOrEmpty(path) && !path.equals("data://")) {
            sb.append("    path: ").append(path).append("\n");
        }
        if (!data.isEmpty()) {
            sb.append("    data: |\n").append(reindent(data.trim(), 8)).append("\n");
        }
        if (!sequence.isEmpty()) {
                sb.append("    sequence: ");
                Joiner.on(", ").appendTo(sb, sequence);
                sb.append("\n");
        }
    }

    public RestxSpec.GivenCleaner run(final ImmutableMap<String, String> params) {
        try {
            MongoClientURI mongoClientURI = new MongoClientURI(
                    checkNotNull(params.get(DB_URI),
                            DB_URI + " param is required"));
            Jongo jongo = new Jongo(new MongoClient(mongoClientURI).getDB(mongoClientURI.getDatabase()));
            Stopwatch stopwatch = new Stopwatch().start();
            MongoCollection collection = jongo.getCollection(getCollection());
            Iterable<String> items = Splitter.on("\n").trimResults().omitEmptyStrings().split(getData());
            int count = 0;
            for (String item : items) {
                collection.insert(item);
                count++;
            }
            System.out.printf("imported %s[%d] -- %s%n", getCollection(), count, stopwatch.stop().toString());

            final UnmodifiableIterator<String> it = getSequence().iterator();
            final CollectionSequence iteratingSequence = new CollectionSequence() {
                @Override
                public Optional<String> next() {
                    return it.hasNext() ? Optional.of(it.next()) : Optional.<String>absent();
                }
            };

            final Factory.Query<Mapper> mapperQuery = Factory.Query.byClass(Mapper.class);
            contextLocal(checkNotNull(params.get(CONTEXT_NAME), CONTEXT_NAME + " param is required")).addMachine(
                    FactoryMachineWrapper.from(new StdJongoCollection.JongoCollectionFactory())
                            .withPriority(-10)
                            .withDependencies(mapperQuery)
                            .transformComponents(new Function<Map.Entry<SatisfiedBOM, NamedComponent>, NamedComponent>() {
                                @Override
                                public NamedComponent apply(final Map.Entry<SatisfiedBOM, NamedComponent> input) {
                                    final JongoCollection collection = (JongoCollection) input.getValue().getComponent();
                                    final Mapper mapper = input.getKey().getOne(mapperQuery).get().getComponent();
                                    final ObjectIdUpdater objectIdUpdater = mapper.getObjectIdUpdater();

                                    return new NamedComponent<>(input.getValue().getName(),
                                            new JongoCollection() {
                                                @Override
                                                public String getName() {
                                                    return collection.getName();
                                                }

                                                @Override
                                                public MongoCollection get() {
                                                    MongoCollection mongoCollection = collection.get();
                                                    if (mongoCollection.getName().equals(GivenJongoCollection.this.collection)) {
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
                                                                            public boolean canSetObjectId(Object target) {
                                                                                return objectIdUpdater.canSetObjectId(target);
                                                                            }

                                                                            @Override
                                                                            public void setDocumentGeneratedId(Object target, ObjectId id) {
                                                                                objectIdUpdater.setDocumentGeneratedId(target,
                                                                                        new ObjectId(iteratingSequence.next().or(id.toString())));
                                                                            }
                                                                        };
                                                                    }

                                                                    @Override
                                                                    public QueryFactory getQueryFactory() {
                                                                        return mapper.getQueryFactory();
                                                                    }
                                                                });
                                                    }
                                                    return mongoCollection;
                                                }
                                            });
                                }
                            }).build());


            return new RestxSpec.GivenCleaner() {
                @Override
                public void cleanUp() {
                    try {
                        MongoClientURI mongoClientURI = new MongoClientURI(
                                checkNotNull(params.get(DB_URI),
                                        DB_URI + " param is required"));
                        Jongo jongo = new Jongo(new MongoClient(mongoClientURI).getDB(mongoClientURI.getDatabase()));
                        Stopwatch stopwatch = new Stopwatch().start();
                        jongo.getCollection(getCollection()).drop();
                        System.out.printf("dropped %s -- %s%n", getCollection(), stopwatch.stop().toString());
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


    public String getCollection() {
        return collection;
    }

    public String getPath() {
        return path;
    }

    public String getData() {
        return data;
    }

    public ImmutableList<String> getSequence() {
        return sequence;
    }

    public GivenJongoCollection addSequenceId(String id) {
        return new GivenJongoCollection(collection, path, data,
                ImmutableList.<String>builder().addAll(sequence).add(id).build());
    }
}
