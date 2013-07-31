package restx.tests;

/**
 * User: xavierhanin
 * Date: 7/31/13
 * Time: 11:04 PM
 */
public class TestResult {
    public static enum Status { SUCCESS, FAILURE, ERROR }

    private TestResultSummary summary;
    private String stdOut;
    private String stdErr;

    public TestResultSummary getSummary() {
        return summary;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public TestResult setSummary(final TestResultSummary summary) {
        this.summary = summary;
        return this;
    }

    public TestResult setStdOut(final String stdOut) {
        this.stdOut = stdOut;
        return this;
    }

    public TestResult setStdErr(final String stdErr) {
        this.stdErr = stdErr;
        return this;
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "summary=" + summary +
                ", stdOut='" + stdOut + '\'' +
                ", stdErr='" + stdErr + '\'' +
                '}';
    }
}
