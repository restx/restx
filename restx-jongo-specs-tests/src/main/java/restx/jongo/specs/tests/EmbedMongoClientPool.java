package restx.jongo.specs.tests;

import com.mongodb.MongoClientURI;
import org.testcontainers.containers.MongoDBContainer;

import java.util.HashSet;
import java.util.Set;

class EmbedMongoClientPool {

    private final Set<Object> clients = new HashSet<>();
    private final MongoDBContainer mongoDBContainer;
    private final Object lock = new Object();
    private boolean isStarted;

    EmbedMongoClientPool(MongoDBContainer mongoDBContainer) {
        this.mongoDBContainer = mongoDBContainer;
    }

    public String getConnectionString() {
        return mongoDBContainer.getConnectionString();
    }

    public MongoClientURI getMongoUri() {
        return new MongoClientURI(getConnectionString());
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
        mongoDBContainer.start();
        isStarted = true;
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
            mongoDBContainer.stop();
            isStarted = false;
        }
    }
}

