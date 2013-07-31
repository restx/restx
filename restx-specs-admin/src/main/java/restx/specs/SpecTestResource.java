package restx.specs;

import com.google.common.base.Optional;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.factory.When;
import restx.tests.RestxSpecTestServer;
import restx.tests.TestRequest;
import restx.tests.TestResult;
import restx.tests.TestResultSummary;

/**
 * User: xavierhanin
 * Date: 7/31/13
 * Time: 10:24 PM
 */
@Component @RestxResource
@When(name="restx.mode", value="test")
public class SpecTestResource {
    private final RestxSpecTestServer.RunningServer server;

    public SpecTestResource(RestxSpecTestServer.RunningServer server) {
        this.server = server;
    }

    @POST("/@/tests/requests")
    public TestRequest submitTestRequest(TestRequest testRequest) {
        return server.submitTestRequest(testRequest);
    }

    @GET("/@/tests/requests/{key}")
    public Optional<TestRequest> getTestRequestByKey(String key) {
        return server.getRequestByKey(key);
    }

    @GET("/@/tests/results/summaries")
    public Iterable<TestResultSummary> findCurrentTestResults() {
        return server.findCurrentTestResults();
    }

    @GET("/@/tests/results/{key}")
    public Optional<TestResult> getTestResultByKey(String key) {
        return server.getResultByKey(key);
    }
}
