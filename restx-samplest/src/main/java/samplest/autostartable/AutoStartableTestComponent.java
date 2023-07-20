package samplest.autostartable;

import com.google.common.base.Optional;
import restx.RestxMainRouter;
import restx.RestxMainRouterFactory;
import restx.factory.AutoStartable;
import restx.factory.Component;

import javax.inject.Named;

/**
 * Date: 1/12/13
 * Time: 14:21
 */
@Component
public class AutoStartableTestComponent implements AutoStartable, AutoCloseable {
    private static int closed;
    private static int started;
    private static int instanciated;
    private final String serverId;
    private final String baseUrl;

    private int called;
    private Optional<RestxMainRouter> router;

    public AutoStartableTestComponent(@Named("restx.server.id") String serverId,
                                      @Named("restx.server.baseUrl") String baseUrl) {
        this.serverId = serverId;
        this.baseUrl = baseUrl;

        instanciated++;
    }

    public static int getClosed() {
        return closed;
    }

    public static void clear() {
        started = instanciated = closed = 0;
    }

    public static int getStarted() {
        return started;
    }

    public static int getInstanciated() {
        return instanciated;
    }

    public int getCalled() {
        return called;
    }


    @Override
    public void close() throws Exception {
        closed++;
    }

    @Override
    public void start() {
        started++;
        router = RestxMainRouterFactory.getInstance(serverId);
    }

    public void call() {
        this.called++;
    }

    public String getServerId() {
        return serverId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Optional<RestxMainRouter> getRouter() {
        return router;
    }
}
