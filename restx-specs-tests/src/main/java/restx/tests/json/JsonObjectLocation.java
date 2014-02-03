package restx.tests.json;

import com.fasterxml.jackson.core.JsonLocation;

/**
* Date: 3/2/14
* Time: 22:47
*/
public class JsonObjectLocation {
    private final String source;
    private final JsonLocation from;
    private final JsonLocation to;

    JsonObjectLocation(String source, JsonLocation from, JsonLocation to) {
        this.source = source;
        this.from = from;
        this.to = to;
    }

    public JsonLocation getFrom() {
        return from;
    }

    public JsonLocation getTo() {
        return to;
    }

    public String getJson() {
        return source.substring((int) from.getCharOffset() - 1, (int) to.getCharOffset());
    }

    @Override
    public String toString() {
        return "Location{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
