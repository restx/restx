package restx.jongo;

import com.mongodb.DBObject;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.ResultHandler;

/**
 * User: xavierhanin
 * Date: 1/28/13
 * Time: 3:00 PM
 */
public class Counter {
    private final MongoCollection counters;
    private final String counter;

    public Counter(Jongo jongo, String counter) {
        this.counters = jongo.getCollection("counters");
        this.counter = counter;
    }

    public long next() {
        Number c = (Number) counters
                .findAndModify("{ _id: # }", counter)
                .with("{ $inc: { seq: 1 } }")
                .returnNew().map(new ResultHandler<Object>() {
                    @Override
                    public Object map(DBObject result) {
                        return result.get("seq");
                    }
                });
        return c.longValue();
    }
}
