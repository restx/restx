package restx.specs;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.ResourcesRoute;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.RestxRouter;
import restx.StdRestxRequestMatcher;
import restx.StdRoute;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 3/18/13
 * Time: 9:37 PM
 */
public class SpecRecorderRoute extends RestxRouter {
    public SpecRecorderRoute(final RestxSpecRecorder.Repository recordedSpecsRepository,
                             final RestxSpec.StorageSettings storageSettings) {
        super("SpecRecorderRouter",
                new ResourcesRoute("RecorderUIRoute", "/@/ui/recorder/", "restx.specs.recorder", ImmutableMap.of("", "index.html")),
                new StdRoute("RecorderRoute", new StdRestxRequestMatcher("GET", "/@/recorders")) {
                    @Override
                    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        resp.setContentType("application/json");
                        List<String> data = Lists.newArrayList();
                        for (RestxSpecRecorder.RecordedSpec spec : recordedSpecsRepository.getRecordedSpecs()) {
                            data.add(String.format("{ \"id\": \"%03d\", \"method\": \"%s\", \"path\": \"%s\", \"recordTime\": \"%s\", \"duration\": %d, " +
                                    "\"capturedItems\": %d, \"capturedRequestSize\": %d, \"capturedResponseSize\": %d }",
                                    spec.getId(), spec.getMethod(), spec.getPath(), spec.getRecordTime(), spec.getDuration().getMillis(),
                                    spec.getCapturedItems(), spec.getCapturedRequestSize(), spec.getCapturedResponseSize()));
                        }
                        resp.getWriter().print("[\n");
                        Joiner.on(",\n").appendTo(resp.getWriter(), data);
                        resp.getWriter().print("\n]");
                    }
                },

                new StdRoute("RecorderRecord", new StdRestxRequestMatcher("GET", "/@/recorders/{id}")) {
                    @Override
                    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        int id = Integer.parseInt(match.getPathParam("id"));
                        for (RestxSpecRecorder.RecordedSpec spec : recordedSpecsRepository.getRecordedSpecs()) {
                            if (spec.getId() == id) {
                                resp.setContentType("text/yaml");
                                resp.getWriter().println(spec.getSpec().toString());
                                return;
                            }
                        }

                        notFound(match, resp);
                    }
                },

                new StdRoute("RecorderRecordStorage", new StdRestxRequestMatcher("POST", "/@/recorders/storage/{id}")) {
                    @Override
                    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        int id = Integer.parseInt(match.getPathParam("id"));
                        for (RestxSpecRecorder.RecordedSpec spec : recordedSpecsRepository.getRecordedSpecs()) {
                            if (spec.getId() == id) {
                                Optional<String> path = req.getQueryParam("path");
                                Optional<String> title = req.getQueryParam("title");

                                RestxSpec.Storage storage = RestxSpec.Storage.with(storageSettings);

                                File destFile = storage.store(
                                        spec.getSpec()
                                            .withTitle(title)
                                            .withPath(storage.buildPath(path, title.or(spec.getSpec().getTitle()))));

                                resp.setContentType("text/plain");
                                resp.getWriter().println(destFile.getAbsolutePath());
                                return;
                            }
                        }

                        notFound(match, resp);
                    }
                }
            );
    }

}
