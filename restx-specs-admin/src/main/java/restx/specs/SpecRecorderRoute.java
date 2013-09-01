package restx.specs;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import restx.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 3/18/13
 * Time: 9:37 PM
 */
public class SpecRecorderRoute extends RestxRouter {
    public SpecRecorderRoute(final RestxSpecRecorder specRecorder) {
        super("SpecRecorderRouter",
                new ResourcesRoute("RecorderUIRoute", "/@/ui/recorder/", "restx.specs.recorder", ImmutableMap.of("", "index.html")),
                new StdRoute("RecorderRoute", new StdRouteMatcher("GET", "/@/recorders")) {
                    @Override
                    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        resp.setContentType("application/json");
                        List<String> data = Lists.newArrayList();
                        for (RestxSpecRecorder.RecordedSpec spec : specRecorder.getRecordedSpecs()) {
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

                new StdRoute("RecorderRecord", new StdRouteMatcher("GET", "/@/recorders/{id}")) {
                    @Override
                    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        int id = Integer.parseInt(match.getPathParams().get("id"));
                        for (RestxSpecRecorder.RecordedSpec spec : specRecorder.getRecordedSpecs()) {
                            if (spec.getId() == id) {
                                resp.setContentType("text/yaml");
                                resp.getWriter().println(spec.getSpec().toString());
                                return;
                            }
                        }

                        notFound(match, resp);
                    }
                },

                new StdRoute("RecorderRecordStorage", new StdRouteMatcher("POST", "/@/recorders/storage/{id}")) {
                    @Override
                    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        int id = Integer.parseInt(match.getPathParams().get("id"));
                        for (RestxSpecRecorder.RecordedSpec spec : specRecorder.getRecordedSpecs()) {
                            if (spec.getId() == id) {
                                Optional<String> path = req.getQueryParam("path");
                                Optional<String> title = req.getQueryParam("title");

                                File destFile = spec.getSpec().store(path, title);

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
