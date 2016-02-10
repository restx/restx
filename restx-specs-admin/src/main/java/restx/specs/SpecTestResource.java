package restx.specs;

import com.google.common.base.Optional;
import restx.RestxLogLevel;
import restx.admin.AdminModule;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.annotations.Verbosity;
import restx.factory.Component;
import restx.factory.When;
import restx.security.RolesAllowed;
import restx.tests.RestxSpecTestServer;
import restx.tests.TestRequest;
import restx.tests.TestResult;
import restx.tests.TestResultSummary;

/**
 * User: xavierhanin
 * Date: 7/31/13
 * Time: 10:24 PM
 */
@Component @RestxResource(group = "restx-admin")
@When(name="restx.mode", value="infinirest")
public class SpecTestResource {
    private final RestxSpecTestServer.RunningServer server;

    public SpecTestResource(RestxSpecTestServer.RunningServer server) {
        this.server = server;
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @POST("/@/tests/requests")
    public TestRequest submitTestRequest(TestRequest testRequest) {
        return server.submitTestRequest(testRequest);
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @GET("/@/tests/requests/{key}")
    public Optional<TestRequest> getTestRequestByKey(String key) {
        return server.getRequestByKey(key);
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @GET("/@/tests/results/summaries")
    @Verbosity(RestxLogLevel.QUIET)
    public Iterable<TestResultSummary> findCurrentTestResults() {
        return server.findCurrentTestResults();
    }

    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    @GET("/@/tests/results/{key}")
    public Optional<TestResult> getTestResultByKey(String key) {
        return server.getResultByKey(key);
    }
}
