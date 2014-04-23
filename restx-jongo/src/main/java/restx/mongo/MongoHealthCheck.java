package restx.mongo;

import com.mongodb.Mongo;
import restx.common.metrics.api.health.HealthCheck;

public class MongoHealthCheck implements HealthCheck {
    private Mongo mongo;

    public MongoHealthCheck(Mongo mongo) {
        this.mongo = mongo;
    }

    @Override
    public void check() throws Exception {
        //throws an exception if unhealthy
        mongo.getDatabaseNames();
    }

}
