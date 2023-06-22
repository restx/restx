package restx.mongo;

import com.mongodb.MongoClient;
import restx.common.metrics.api.health.HealthCheck;

public class MongoHealthCheck implements HealthCheck {
    private MongoClient mongo;

    public MongoHealthCheck(MongoClient mongo) {
        this.mongo = mongo;
    }

    @Override
    public void check() throws Exception {
        //throws an exception if unhealthy
        mongo.listDatabaseNames();
    }

}
