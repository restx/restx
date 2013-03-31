package restx;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import restx.common.Tpl;
import restx.factory.Component;

import java.io.IOException;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 2/9/13
 * Time: 12:44 AM
 */
@Component
public class MonitorRoute implements RestxRoute {

    @Override
    public boolean route(RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        if ("GET".equals(req.getHttpMethod()) && "/@/monitor".equals(req.getRestxPath())) {
            Tpl tpl = new Tpl(MonitorRoute.class, "monitor.html");
            resp.setContentType("text/html");

            List<String> data = Lists.newArrayList();
            int i = 0;
            for (Monitor monitor : MonitorFactory.getRootMonitor().getMonitors()) {
                if (!monitor.getLabel().endsWith("/@/monitor")) {
                    data.add(String.format(
                        "{ id: %s, label: \"%s\", \"hits\": %.2f, \"avg\": %.2f, \"lastVal\": %.2f, \"min\": %.2f, \"max\": %.2f," +
                                " \"active\": %.2f, \"avgActive\": %.2f, \"maxActive\": %.2f, \"firstAccess\": \"%tF %tT\", \"lastAccess\": \"%tF %tT\" }",
                        i++, monitor.getLabel(), monitor.getHits(), monitor.getAvg(), monitor.getLastValue(), monitor.getMin(), monitor.getMax(),
                            monitor.getActive(), monitor.getAvgActive(), monitor.getMaxActive(),
                            monitor.getFirstAccess(), monitor.getFirstAccess(),
                            monitor.getLastAccess(), monitor.getLastAccess()));
                }
            }

            resp.getWriter().println(tpl.bind(ImmutableMap.of("data", Joiner.on(",\n").join(data))));
            return true;
        }

        return false;
    }
}
