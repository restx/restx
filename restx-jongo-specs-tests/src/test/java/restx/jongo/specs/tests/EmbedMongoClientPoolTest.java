package restx.jongo.specs.tests;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EmbedMongoClientPoolTest {

    @Test
    public void should_checkIn_subscribe_object_reference_in_client_pool() {
        // Given
        MongodExecutable executable = mock(MongodExecutable.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

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
        MongodExecutable executable = mock(MongodExecutable.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

        Object client = new Object();

        // When
        pool.checkIn(client);

        // Then
        verify(executable).start();
    }

    @Test
    public void should_checkIn_not_start_mongo_executable_after_second_checkIn() throws IOException {
        // Given
        MongodExecutable executable = mock(MongodExecutable.class);
        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

        Object firstClient = new Object();
        Object secondClient = new Object();

        // When
        pool.checkIn(firstClient);
        pool.checkIn(secondClient);

        // Then
        verify(executable).start();
    }

    @Test
    public void should_checkOut_remove_client_from_pool() throws IOException {
        // Given
        MongodExecutable executable = newMongodExecutable();
        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

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
        MongodExecutable executable = newMongodExecutable();
        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

        Object client = new Object();
        pool.checkIn(client);

        // When
        pool.checkOut(client);

        // Then
        verify(executable).stop();
    }

    @Test
    public void should_checkOut_not_stop_mongo_executable_when_other_clients_are_in_the_pool() throws IOException {
        // Given
        MongodExecutable executable = newMongodExecutable();
        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

        Object firstClient = new Object();
        pool.checkIn(firstClient);

        Object secondClient = new Object();
        pool.checkIn(secondClient);

        int expectedNumberOfInvocations = 0;

        // When
        pool.checkOut(firstClient);

        // Then
        verify(executable, times(expectedNumberOfInvocations)).stop();
    }

    private MongodExecutable newMongodExecutable() throws IOException {
        MongodExecutable executable = mock(MongodExecutable.class);
        when(executable.start()).thenReturn(mock(MongodProcess.class));
        return executable;
    }

    @Test
    public void should_checkOut_stop_mongo_process_after_last_checkout() throws IOException {
        // Given
        MongodProcess process = mock(MongodProcess.class);

        MongodExecutable executable = mock(MongodExecutable.class);
        when(executable.start()).thenReturn(process);

        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

        Object client = new Object();
        pool.checkIn(client);

        // When
        pool.checkOut(client);

        // Then
        verify(process).stop();
    }

    @Test
    public void should_checkIn_start_again_database_when_stopped_after_checkOut() throws IOException {
        // Given
        MongodExecutable executable = newMongodExecutable();
        EmbedMongoClientPool pool = new EmbedMongoClientPool(executable);

        Object client = new Object();

        // When
        pool.checkIn(client);
        pool.checkOut(client);
        pool.checkIn(client);

        // Then
        verify(executable, times(2)).start();
    }
}
