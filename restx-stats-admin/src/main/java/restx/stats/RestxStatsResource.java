package restx.stats;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

/**
 * Date: 4/5/14
 * Time: 11:08
 */
@RestxResource(group = "restx-admin")
@Component
public class RestxStatsResource {
    private final RestxStatsCollector collector;

    public RestxStatsResource(RestxStatsCollector collector) {
        this.collector = collector;
    }

    @GET("/@/restx-stats")
    public RestxStats getRestxStats() {
        return collector.getStats();
    }
}
