package restx.tests;

import org.joda.time.DateTime;

/**
 * User: xavierhanin
 * Date: 7/31/13
 * Time: 10:33 PM
 */
public class TestRequest {
    public static enum Status {
        QUEUED, RUNNING, DONE, CANCELLED;
    }

    private String key;
    private String test;
    private Status status;
    private DateTime requestTime;

    /**
     * the key of the corresponding TestResult once this test request has been processed (status == DONE).
     */
    private String testResultKey;

    public String getKey() {
        return key;
    }

    public String getTest() {
        return test;
    }

    public Status getStatus() {
        return status;
    }

    public String getTestResultKey() {
        return testResultKey;
    }

    public DateTime getRequestTime() {
        return requestTime;
    }

    public TestRequest setKey(final String key) {
        this.key = key;
        return this;
    }

    public TestRequest setTest(final String test) {
        this.test = test;
        return this;
    }

    public TestRequest setStatus(final Status status) {
        this.status = status;
        return this;
    }

    public TestRequest setTestResultKey(final String testResultKey) {
        this.testResultKey = testResultKey;
        return this;
    }

    public TestRequest setRequestTime(final DateTime requestTime) {
        this.requestTime = requestTime;
        return this;
    }


    @Override
    public String toString() {
        return "TestRequest{" +
                "key='" + key + '\'' +
                ", test='" + test + '\'' +
                ", status=" + status +
                ", requestTime=" + requestTime +
                ", testResultKey='" + testResultKey + '\'' +
                '}';
    }
}