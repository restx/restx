package restx.jongo.specs.tests;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import restx.mongo.MongoModule;

import java.io.Closeable;
import java.util.Objects;

import static restx.jongo.specs.tests.MongoVersion.DEFAULT_MONGO_VERSION;

public final class MongoRestxSpecTestsListener extends RunListener implements Closeable {

    private final EmbedMongoFactory factory;
    private EmbedMongoClientPool pool;

    public MongoRestxSpecTestsListener() {
        this.factory = new EmbedMongoFactory();
    }

    MongoRestxSpecTestsListener(EmbedMongoFactory factory) {
        this.factory = factory;
    }

    @Override
    public void testRunStarted(Description description) {
        pool = factory.getEmbedMongoClientPool(DEFAULT_MONGO_VERSION);
        System.setProperty(MongoModule.MONGO_URI, pool.getConnectionString());
        pool.checkIn(this);
    }

    @Override
    public void testRunFinished(Result result) {
        close();
    }

    @Override
    public void close() {
        System.clearProperty(MongoModule.MONGO_URI);
        if (Objects.nonNull(pool)) {
            pool.checkOut(this);
        }
    }
}
