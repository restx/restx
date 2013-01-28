package restx.jongo;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import static restx.jongo.Jongos.singleField;

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
        Number c = counters
                .findAndModify("{ _id: # }", counter)
                .with("{ $inc: { seq: 1 } }")
                .returnNew().map(singleField("seq", Number.class));
        return c.longValue();
    }
}
