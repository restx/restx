package restx.jongo.specs.tests;

import com.mongodb.MongoClientURI;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import restx.mongo.MongoModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MongoRestxSpecTestsListenerTest {

    @Test
    public void should_testRunStarted_be_instance_of_RunListener() {
        // Given
        // When
        try (MongoRestxSpecTestsListener output = new MongoRestxSpecTestsListener()) {
            // Then
            assertThat(output).isInstanceOf(RunListener.class);
        }
    }

    @Test
    public void should_testRunStarted_set_mongo_uri_property() {
        // Given
        try (MongoRestxSpecTestsListener listener = new MongoRestxSpecTestsListener()) {
            Description description = mock(Description.class);
            String mongoUriKey = MongoModule.MONGO_URI;

            // When
            listener.testRunStarted(description);

            // Then
            String output = System.getProperty(mongoUriKey);
            assertThat(output).isNotNull();
        }
    }

    @Test
    public void should_testRunStarted_prepare_a_mongo_container() {
        // Given
        EmbedMongoClientPool pool = mock(EmbedMongoClientPool.class);
        EmbedMongoFactory factory = newEmbedMongoFactory(pool);
        try (MongoRestxSpecTestsListener listener = new MongoRestxSpecTestsListener(factory)) {
            Description description = mock(Description.class);

            // When
            listener.testRunStarted(description);

            // Then
            verify(factory).getEmbedMongoClientPool(any(String.class));
        }
    }

    @Test
    public void should_testRunStarted_checkin_the_pool() {
        // Given
        EmbedMongoClientPool pool = mock(EmbedMongoClientPool.class);

        EmbedMongoFactory factory = newEmbedMongoFactory(pool);

        try (MongoRestxSpecTestsListener listener = new MongoRestxSpecTestsListener(factory)) {
            Description description = mock(Description.class);

            // When
            listener.testRunStarted(description);

            // Then
            verify(pool).checkIn(listener);
        }
    }

    private EmbedMongoFactory newEmbedMongoFactory(EmbedMongoClientPool pool) {
        final String connectionString = "mongodb://localhost:21017";
        MongoClientURI mongoClientURI = mock(MongoClientURI.class);
        when(mongoClientURI.getURI()).thenReturn(connectionString);

        when(pool.getMongoUri()).thenReturn(mongoClientURI);
        when(pool.getConnectionString()).thenReturn(connectionString);

        EmbedMongoFactory factory = mock(EmbedMongoFactory.class);
        when(factory.getEmbedMongoClientPool(any(String.class))).thenReturn(pool);
        return factory;
    }

    @Test
    public void should_close_unset_mongo_uri_property() {
        // Given
        MongoRestxSpecTestsListener listener = new MongoRestxSpecTestsListener();

        String mongoUriKey = MongoModule.MONGO_URI;
        System.setProperty(mongoUriKey, "a_mongo_uri");

        // When
        listener.close();

        // Then
        String output = System.getProperty(mongoUriKey);
        assertThat(output).isNull();
    }

    @Test
    public void should_close_checkout_from_pool() {
        // Given
        EmbedMongoClientPool pool = mock(EmbedMongoClientPool.class);

        EmbedMongoFactory factory = newEmbedMongoFactory(pool);

        MongoRestxSpecTestsListener listener = new MongoRestxSpecTestsListener(factory);
        listener.testRunStarted(mock(Description.class));

        // When
        listener.close();

        // Then
        verify(pool).checkOut(listener);
    }

    @Test
    public void should_testRunFinished_checkout_from_pool() {
        // Given
        EmbedMongoClientPool pool = mock(EmbedMongoClientPool.class);

        EmbedMongoFactory factory = newEmbedMongoFactory(pool);

        MongoRestxSpecTestsListener listener = new MongoRestxSpecTestsListener(factory);
        listener.testRunStarted(mock(Description.class));

        Result result = mock(Result.class);

        // When
        listener.testRunFinished(result);

        // Then
        verify(pool).checkOut(listener);
    }
}
