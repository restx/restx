package restx.jongo.specs.tests;

import com.mongodb.MongoClientURI;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class EmbedMongoFactory {

    private static final Map<Version.Main, PoolKey> keys = new ConcurrentHashMap<>();
    private static final Map<PoolKey, EmbedMongoClientPool> pools = new ConcurrentHashMap<>();

    private static synchronized void newPoolKey(Version.Main version) throws IOException {
        if (Objects.isNull(keys.get(version))) {
            InetAddress addr = Network.getLocalHost();
            int port = Network.getFreeServerPort(addr);
            String uri = "mongodb://" + addr.getHostName() + ":" + port;

            MongoClientURI mongoClientURI = new MongoClientURI(uri);
            PoolKey key = new PoolKey(version, mongoClientURI);
            keys.put(version, key);
        }
    }

    PoolKey getPoolKey(Version.Main version) throws IOException {
        if (Objects.isNull(keys.get(version))) {
            newPoolKey(version);
        }
        return keys.get(version);
    }

    synchronized EmbedMongoClientPool getEmbedMongoClientPool(MongodStarter starter, PoolKey key) {
        EmbedMongoClientPool pool = pools.get(key);

        if (Objects.nonNull(pool)) {
            return pool;
        }

        return newPool(starter, key);
    }

    private EmbedMongoClientPool newPool(MongodStarter starter, PoolKey key) {
        try {
            MongodExecutable executable = prepareExecutable(starter, key);
            EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);
            pools.put(key, pool);
            return pool;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private MongodExecutable prepareExecutable(MongodStarter starter, PoolKey key)
            throws IOException {

        int port = Integer.parseInt(key.getUri().getURI().split(":")[2]);
        Net net = new Net(port, false);

        IMongodConfig config = new MongodConfigBuilder()
                .version(key.getVersion())
                .net(net)
                .build();

        return starter.prepare(config);
    }
}
