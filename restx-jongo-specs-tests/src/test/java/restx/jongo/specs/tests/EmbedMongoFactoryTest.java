package restx.jongo.specs.tests;

import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbedMongoFactoryTest {

    @Test
    public void should_getPoolKey_return_the_same_instance_when_called_twice_with_same_version() {
        // Given
        Version.Main version = Version.Main.PRODUCTION;

        EmbedMongoFactory factory = new EmbedMongoFactory();
        PoolKey expectedOutput = factory.getPoolKey(version);

        EmbedMongoFactory anotherFactory = new EmbedMongoFactory();

        // When
        PoolKey output = anotherFactory.getPoolKey(version);

        // Then
        assertThat(output).isSameAs(expectedOutput);
    }

    @Test
    public void should_getPoolKey_return_the_different_uri_when_called_twice_with_different_version() {
        // Given
        Version.Main production = Version.Main.PRODUCTION;

        EmbedMongoFactory factory = new EmbedMongoFactory();
        PoolKey unexpectedOutput = factory.getPoolKey(production);

        Version.Main olderVersion = Version.Main.V3_4;
        EmbedMongoFactory anotherFactory = new EmbedMongoFactory();

        // When
        PoolKey output = anotherFactory.getPoolKey(olderVersion);

        // Then
        assertThat(output).isNotSameAs(unexpectedOutput);
    }

    @Test
    public void should_getEmbedMongoClientPool_return_the_same_instance_when_called_with_same_param_MongodExecutable() {

        // Given
        EmbedMongoFactory factory = new EmbedMongoFactory();

        MongodStarter starter = MongodStarter.getDefaultInstance();
        Version.Main version = Version.Main.PRODUCTION;
        PoolKey key = factory.getPoolKey(version);

        EmbedMongoClientPool expectedOutput = factory.getEmbedMongoClientPool(starter, key);

        EmbedMongoFactory anotherFactory = new EmbedMongoFactory();

        // When
        EmbedMongoClientPool output = anotherFactory.getEmbedMongoClientPool(starter, key);

        // Then
        assertThat(output).isSameAs(expectedOutput);
    }

    @Test
    public void should_getEmbedMongoClientPool_return_the_different_instance_when_called_with_different_key() {

        // Given
        EmbedMongoFactory factory = new EmbedMongoFactory();

        MongodStarter starter = MongodStarter.getDefaultInstance();
        Version.Main version = Version.Main.PRODUCTION;
        PoolKey key = factory.getPoolKey(version);

        EmbedMongoClientPool unexpectedOutput = factory.getEmbedMongoClientPool(starter, key);

        EmbedMongoFactory anotherFactory = new EmbedMongoFactory();
        Version.Main anotherVersion = Version.Main.V3_4;
        PoolKey anotherKey = factory.getPoolKey(anotherVersion);

        // When
        EmbedMongoClientPool output = anotherFactory.getEmbedMongoClientPool(starter, anotherKey);

        // Then
        assertThat(output).isNotSameAs(unexpectedOutput);
    }
}