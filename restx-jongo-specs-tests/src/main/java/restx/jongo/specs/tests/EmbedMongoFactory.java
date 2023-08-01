package restx.jongo.specs.tests;

import org.testcontainers.containers.MongoDBContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class EmbedMongoFactory {

    private static final Map<String, EmbedMongoClientPool> pools = new ConcurrentHashMap<>();

    EmbedMongoClientPool getEmbedMongoClientPool(String version) {
        return pools.computeIfAbsent(version, this::newPool);
    }

    private EmbedMongoClientPool newPool(String version) {
        return new EmbedMongoClientPool(new MongoDBContainer("mongo:" + version));
    }
}
