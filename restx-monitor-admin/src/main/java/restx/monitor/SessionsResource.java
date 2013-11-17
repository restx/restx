package restx.monitor;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.Sessions;

import java.util.Map;

/**
 * Date: 17/11/13
 * Time: 01:03
 */
@RestxResource(group = "restx-admin") @Component
public class SessionsResource {
    private final Sessions sessions;

    public SessionsResource(Sessions sessions) {
        this.sessions = sessions;
    }

    @GET("/@/sessionStats")
    public Iterable<Sessions.SessionData> metrics() {
        return sessions.getAll().values();
    }
}
