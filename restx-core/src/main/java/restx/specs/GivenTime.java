package restx.specs;

import org.joda.time.DateTime;

/**
* @author xavierhanin
*/
public class GivenTime implements Given {

    private final DateTime time;

    public GivenTime(DateTime time) {
        this.time = time;
    }

    public DateTime getTime() {
        return time;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append("  - time: ").append(time.toString()).append(System.lineSeparator());
    }
}
