package restx.jongo.specs.tests;

import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EmbedMongoClientPoolTest {

    @Test
    public void should_checkIn_subscribe_object_reference_in_client_pool() {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object client = new Object();

        // When
        pool.checkIn(client);

        // Then
        boolean output = pool.isCheckedIn(client);
        assertThat(output).isTrue();
    }

    @Test
    public void should_checkIn_start_mongo_executable_after_first_checkIn() throws IOException {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object client = new Object();

        // When
        pool.checkIn(client);

        // Then
        verify(mongoDBContainer).start();
    }

    @Test
    public void should_checkIn_not_start_mongo_executable_after_second_checkIn() throws IOException {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object firstClient = new Object();
        Object secondClient = new Object();

        // When
        pool.checkIn(firstClient);
        pool.checkIn(secondClient);

        // Then
        verify(mongoDBContainer).start();
    }

    @Test
    public void should_checkOut_remove_client_from_pool() throws IOException {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object client = new Object();
        pool.checkIn(client);

        // When
        pool.checkOut(client);

        // Then
        boolean output = pool.isCheckedIn(client);
        assertThat(output).isFalse();
    }

    @Test
    public void should_checkOut_stop_mongo_executable_after_last_checkout() throws IOException {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object client = new Object();
        pool.checkIn(client);

        // When
        pool.checkOut(client);

        // Then
        verify(mongoDBContainer).stop();
    }

    @Test
    public void should_checkOut_not_stop_mongo_executable_when_other_clients_are_in_the_pool() throws IOException {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object firstClient = new Object();
        pool.checkIn(firstClient);

        Object secondClient = new Object();
        pool.checkIn(secondClient);

        int expectedNumberOfInvocations = 0;

        // When
        pool.checkOut(firstClient);

        // Then
        verify(mongoDBContainer, times(expectedNumberOfInvocations)).stop();
    }

    @Test
    public void should_checkOut_stop_mongo_process_after_last_checkout() throws IOException {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object client = new Object();
        pool.checkIn(client);

        // When
        pool.checkOut(client);

        // Then
        verify(mongoDBContainer).stop();
    }

    @Test
    public void should_checkIn_start_again_database_when_stopped_after_checkOut() throws IOException {
        // Given
        MongoDBContainer mongoDBContainer = mock(MongoDBContainer.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(mongoDBContainer);

        Object client = new Object();

        // When
        pool.checkIn(client);
        pool.checkOut(client);
        pool.checkIn(client);

        // Then
        verify(mongoDBContainer, times(2)).start();
    }
}
