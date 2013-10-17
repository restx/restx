package restx.monitor;

import com.google.common.collect.ImmutableMap;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import restx.*;
import restx.factory.Component;

import java.io.IOException;
import java.util.Locale;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 12:44 AM
 */
@Component
public class MonitorRouter extends RestxRouter {
    public MonitorRouter() {
        super("restx-admin", "MonitorRouter",
                new ResourcesRoute("MonitorUIRoute", "/@/ui/monitor",
                    MonitorRouter.class.getPackage().getName(), ImmutableMap.of("", "index.html")),

                new StdRoute("MonitorRoute", new StdRouteMatcher("GET", "/@/monitor")) {
                    @Override
                    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                        resp.setStatus(HttpStatus.OK);
                        resp.setContentType("application/json");
                        resp.getWriter().print("[");
                        int i = 0;
                        for (Monitor monitor : MonitorFactory.getRootMonitor().getMonitors()) {
                            if (monitor.getLabel().endsWith("/@/monitor")) {
                                // not include monitor information on this route itself
                                continue;
                            }
                            if (i != 0) {
                                resp.getWriter().print(",");
                            }
                            resp.getWriter().print(String.format(Locale.ENGLISH,
                                "{ \"id\": %s, \"label\": \"%s\", \"hits\": %.2f, \"avg\": %.2f, \"lastVal\": %.2f, \"min\": %.2f, \"max\": %.2f," +
                                        " \"active\": %.2f, \"avgActive\": %.2f, \"maxActive\": %.2f, \"firstAccess\": \"%tF %tT\", \"lastAccess\": \"%tF %tT\" }",
                                i++, monitor.getLabel(), monitor.getHits(), monitor.getAvg(), monitor.getLastValue(), monitor.getMin(), monitor.getMax(),
                                    monitor.getActive(), monitor.getAvgActive(), monitor.getMaxActive(),
                                    monitor.getFirstAccess(), monitor.getFirstAccess(),
                                    monitor.getLastAccess(), monitor.getLastAccess()));
                        }
                        resp.getWriter().print("]");
                    }
                });
    }
}
