package restx.specs;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.common.Tpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 3/18/13
 * Time: 9:37 PM
 */
public class SpecRecorderRoute implements RestxRoute {
    private final RestxSpecRecorder specRecorder;

    public SpecRecorderRoute(RestxSpecRecorder specRecorder) {
        this.specRecorder = specRecorder;
    }

    @Override
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        if ("GET".equals(req.getHttpMethod()) && "/@/recorder".equals(req.getRestxPath())) {
            Tpl tpl = new Tpl(SpecRecorderRoute.class, "recorder.html");
            resp.setContentType("text/html");

            List<String> data = Lists.newArrayList();
            for (RestxSpecRecorder.RecordedSpec spec : specRecorder.getRecordedSpecs()) {
                data.add(String.format("{ id: \"%03d\", method: \"%s\", path: \"%s\", recordTime: \"%s\", duration: %d, " +
                        "capturedItems: %d, capturedRequestSize: %d, capturedResponseSize: %d }",
                        spec.getId(), spec.getMethod(), spec.getPath(), spec.getRecordTime(), spec.getDuration().getMillis(),
                        spec.getCapturedItems(), spec.getCapturedRequestSize(), spec.getCapturedResponseSize()));
            }

            resp.getWriter().println(tpl.bind(ImmutableMap.of(
                    "baseUrl", req.getBaseUri(),
                    "data", Joiner.on(",\n").join(data))));
            return true;
        } else if ("GET".equals(req.getHttpMethod()) && req.getRestxPath().startsWith("/@/recorder/")) {
            int id = Integer.parseInt(req.getRestxPath().substring("/@/recorder/".length()));
            for (RestxSpecRecorder.RecordedSpec spec : specRecorder.getRecordedSpecs()) {
                if (spec.getId() == id) {
                    resp.setContentType("text/yaml");
                    resp.getWriter().println(spec.getSpec().toString());
                    return true;
                }
            }
            return false;
        } else if ("POST".equals(req.getHttpMethod()) && req.getRestxPath().startsWith("/@/recorder/storage/")) {
            int id = Integer.parseInt(req.getRestxPath().substring("/@/recorder/storage/".length()));
            for (RestxSpecRecorder.RecordedSpec spec : specRecorder.getRecordedSpecs()) {
                if (spec.getId() == id) {
                    String basePath = System.getProperty("restx.recorder.basePath", "specs");
                    Optional<String> path = req.getQueryParam("path");
                    Optional<String> title = req.getQueryParam("title");

                    int endIndex = spec.getPath().indexOf('?');
                    endIndex = endIndex == -1 ? spec.getPath().length() : endIndex;
                    File destFile = new File(basePath + "/" + path.or("") + "/"
                            + title.or(String.format("%03d_%s_%s", spec.getId(), spec.getMethod(), spec.getPath().substring(0, endIndex)))
                                .replace(' ', '_').replace('/', '_') + ".yaml");
                    destFile.getParentFile().mkdirs();
                    Files.append(spec.getSpec().toString(), destFile, Charsets.UTF_8);

                    resp.setContentType("text/plain");
                    resp.getWriter().println(destFile.getAbsolutePath());
                    return true;
                }
            }
            return false;
        }

        return false;
    }
}
