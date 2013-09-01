package restx.specs;

import static restx.common.MoreStrings.indent;

/**
* @author xavierhanin
*/
public class ThenHttpResponse implements Then {
    private final int expectedCode;
    private final String expected;

    public ThenHttpResponse(int expectedCode, String expected) {
        this.expectedCode = expectedCode;
        this.expected = expected;
    }

    public String getExpected() {
        return expected;
    }

    public int getExpectedCode() {
        return expectedCode;
    }

    public void toString(StringBuilder sb) {
        sb.append("    then: |\n");
        if (expectedCode != 200) {
            sb.append("       ").append(expectedCode).append("\n\n");
        }
        sb.append(indent(expected, 8)).append("\n");
    }
}
