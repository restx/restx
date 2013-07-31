package restx.tests;

import org.joda.time.DateTime;

/**
 * User: xavierhanin
 * Date: 7/31/13
 * Time: 11:04 PM
 */
public class TestResultSummary {
    public static enum Status { SUCCESS, FAILURE, ERROR }

    private String key;
    private String name;
    private Status status;
    private DateTime testTime;
    private Long testDuration;

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public DateTime getTestTime() {
        return testTime;
    }

    public Long getTestDuration() {
        return testDuration;
    }

    public TestResultSummary setKey(final String key) {
        this.key = key;
        return this;
    }

    public TestResultSummary setName(final String name) {
        this.name = name;
        return this;
    }

    public TestResultSummary setStatus(final Status status) {
        this.status = status;
        return this;
    }

    public TestResultSummary setTestTime(final DateTime testTime) {
        this.testTime = testTime;
        return this;
    }

    public TestResultSummary setTestDuration(final Long testDuration) {
        this.testDuration = testDuration;
        return this;
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", testTime=" + testTime +
                ", testDuration=" + testDuration +
                '}';
    }
}
