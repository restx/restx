package restx.jongo;

import static restx.jongo.Jongos.singleField;

/**
 * User: xavierhanin
 * Date: 1/28/13
 * Time: 3:00 PM
 */
public class Counter {
    private final JongoCollection counters;
    private final String counter;

    public Counter(JongoCollection counters, String counter) {
        this.counters = counters;
        this.counter = counter;
    }

    public long next() {
        Number c = counters.get()
                .findAndModify("{ _id: # }", counter)
                .with("{ $inc: { seq: 1 } }")
                .returnNew().map(singleField("seq", Number.class));
        return c.longValue();
    }
}
