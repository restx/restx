package restx.jongo.specs.tests;

import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import restx.mongo.MongoModule;

import java.io.Closeable;
import java.util.Objects;

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
        Version.Main version = Version.Main.PRODUCTION;
        PoolKey key = factory.getPoolKey(version);
        System.setProperty(MongoModule.MONGO_URI, key.getUri().getURI());
        MongodStarter starter = MongodStarter.getDefaultInstance();
        pool = factory.getEmbedMongoClientPool(starter, key);
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
