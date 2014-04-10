package samplest.autostartable;

import com.github.kevinsawicki.http.HttpRequest;
import org.junit.Test;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.server.WebServers;
import restx.server.simple.simple.SimpleWebServer;
import restx.tests.HttpTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 27/11/13
 * Time: 21:24
 */
public class AutoStartableTest {
    @Test
    public void should_handle_auto_startable_in_dev_mode() throws Exception {
        Factory.LocalMachines.threadLocal().addMachine(
                new SingletonFactoryMachine<>(-1000, NamedComponent.of(String.class, "restx.mode", "dev")));

        try {
            SimpleWebServer server = SimpleWebServer.builder()
                    .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
            server.start();
            try {
                HttpTestClient client = HttpTestClient.withBaseUrl(server.baseUrl());
                HttpRequest httpRequest = client.GET("/api/autostartable/test");
                assertThat(httpRequest.code()).isEqualTo(200);
                assertThat(httpRequest.body().trim()).isEqualTo(
                        "called: 1 - autostartable: called: 1 started: 1 closed: 0 instanciated: 1" +
                                " serverId: "+server.getServerId()+" baseUrl: "+server.baseUrl()+" routerPresent: true");

                httpRequest = client.GET("/api/autostartable/test");
                assertThat(httpRequest.code()).isEqualTo(200);
                // called should be only one in test mode, components are dropped at each request
                // but autostartable should be reused
                assertThat(httpRequest.body().trim()).isEqualTo(
                        "called: 1 - autostartable: called: 2 started: 1 closed: 0 instanciated: 1" +
                                " serverId: "+server.getServerId()+" baseUrl: "+server.baseUrl()+" routerPresent: true");
            } finally {
                server.stop();
            }
        } finally {
            Factory.LocalMachines.threadLocal().clear();
        }
    }
}
