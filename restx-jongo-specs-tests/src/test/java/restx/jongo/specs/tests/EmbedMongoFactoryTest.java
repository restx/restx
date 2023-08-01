package restx.jongo.specs.tests;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbedMongoFactoryTest {
    @Test
    public void should_getEmbedMongoClientPool_return_the_same_instance_when_called_with_same_param_MongodExecutable() {

        // Given
        EmbedMongoFactory factory = new EmbedMongoFactory();

        EmbedMongoClientPool expectedOutput = factory.getEmbedMongoClientPool(MongoVersion.DEFAULT_MONGO_VERSION);

        EmbedMongoFactory anotherFactory = new EmbedMongoFactory();

        // When
        EmbedMongoClientPool output = anotherFactory.getEmbedMongoClientPool(MongoVersion.DEFAULT_MONGO_VERSION);

        // Then
        assertThat(output).isSameAs(expectedOutput);
    }

    @Test
    public void should_getEmbedMongoClientPool_return_the_different_instance_when_called_with_different_key() {

        // Given
        EmbedMongoFactory factory = new EmbedMongoFactory();

        EmbedMongoClientPool unexpectedOutput = factory.getEmbedMongoClientPool(MongoVersion.DEFAULT_MONGO_VERSION);

        EmbedMongoFactory anotherFactory = new EmbedMongoFactory();

        // When
        EmbedMongoClientPool output = anotherFactory.getEmbedMongoClientPool("5.0");

        // Then
        assertThat(output).isNotSameAs(unexpectedOutput);
    }
}
