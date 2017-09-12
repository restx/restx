package restx.server;

import restx.common.Version;

import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static restx.common.MoreFiles.checkFileExists;
import static restx.common.MoreIO.checkCanOpenSocket;

public abstract class WebServerBase implements WebServer {
    protected static final AtomicLong SERVER_ID = new AtomicLong();

    protected final int port;
    protected final String bindInterface;
    protected final String appBase;
    protected String serverId;

    protected final String serverTypeName;
    protected final String serverGroupId;
    protected final String serverModule;

    protected WebServerBase(String appBase, int port, String bindInterface, String serverTypeName, String serverGroupId, String serverModule) {
        if(appBase != null) {
            checkFileExists(appBase);
        }

        this.port = port;
        this.bindInterface = bindInterface;
        this.appBase = appBase;
        this.serverId = serverTypeName + "#" + SERVER_ID.incrementAndGet();

        this.serverTypeName = serverTypeName;
        this.serverGroupId = serverGroupId;
        this.serverModule = serverModule;
    }

    /**
     * Sets the serverId used by this server.
     *
     * Must not be called when server is started.
     *
     * The serverId is used to uniquely identify the main Factory used by REST main router in this server.
     * It allows to access the Factory with Factory.getInstance(serverId).
     *
     * @param serverId the server id to set. Must be unique in the JVM.
     *
     * @return current server
     */
    public synchronized WebServerBase setServerId(final String serverId) {
        if (isStarted()) {
            throw new IllegalStateException("can't set server id when server is started");
        }
        this.serverId = serverId;
        return this;
    }

    @Override
    public String getServerType() {
        return serverTypeName + " " + Version.getVersion(serverGroupId, serverModule) + ", embedded";
    }

    @Override
    public String getServerId() {
        return serverId;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String baseUrl() {
        // Dunno why, but if I use bindInterface here, some test will fail in ContextParamsTest
        return WebServers.baseUri("127.0.0.1", port);
    }

    @Override
    public synchronized void start() throws Exception {
        checkCanOpenSocket(port);
        WebServers.register(this);

        this._start();
    }

    @Override
    public void startAndAwait() throws Exception {
        start();
        await();
    }

    @Override
    public synchronized void stop() throws Exception {
        this._stop();

        WebServers.unregister(serverId);
    }

    @Override
    public synchronized boolean isStarted() {
        return WebServers.getServerById(serverId).isPresent();
    }

    public abstract void await() throws InterruptedException;
    protected abstract void _start() throws Exception;
    protected abstract void _stop() throws Exception;
}
