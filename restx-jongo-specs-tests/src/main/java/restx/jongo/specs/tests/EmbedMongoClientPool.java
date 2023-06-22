package restx.jongo.specs.tests;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

class EmbedMongoClientPool {

    private final Set<Object> clients = new HashSet<>();

    private final MongodExecutable executable;
    private final Object lock = new Object();
    private MongodProcess process;
    private boolean isStarted;

    EmbedMongoClientPool(MongodExecutable executable) {
        this.executable = executable;
    }

    void checkIn(Object client) {
        synchronized (lock) {
            doCheckIn(client);
        }
    }

    private void doCheckIn(Object client) {
        if (!isStarted) {
            tryStartExecutable();
        }
        clients.add(client);
    }

    private void tryStartExecutable() {
        try {
            process = executable.start();
            isStarted = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    boolean isCheckedIn(Object object) {
        return clients.contains(object);
    }

    void checkOut(Object client) {
        synchronized (lock) {
            doCheckOut(client);
        }
    }

    private void doCheckOut(Object client) {
        clients.remove(client);
        if (clients.isEmpty()) {
            process.stop();
            executable.stop();
            isStarted = false;
        }
    }
}
