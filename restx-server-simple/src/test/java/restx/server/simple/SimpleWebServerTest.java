package restx.server.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Files;
import org.junit.Test;
import restx.*;
import restx.entity.MatchedEntityOutputRoute;
import restx.entity.MatchedEntityRoute;
import restx.http.HttpStatus;
import restx.server.WebServers;
import restx.server.simple.simple.SimpleWebServer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 27/11/13
 * Time: 21:24
 */
public class SimpleWebServerTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void should_handle_simple_routes() throws Exception {
        SimpleWebServer server = SimpleWebServer.builder().setRouter(StdRestxMainRouter.builder()
                .addRouter(RestxRouter.builder()
                        .withMapper(mapper)
                        .GET("/route1/{id}", Map.class, new MatchedEntityOutputRoute() {
                            @Override
                            public Optional route(RestxRequest restxRequest, RestxRequestMatch match) {
                                return Optional.of(ImmutableMap.of("id", match.getPathParam("id")));
                            }
                        })
                        .PUT("/route4", Map.class, Map.class, new MatchedEntityRoute<Map, Map>() {
                            @Override
                            public Optional<Map> route(RestxRequest restxRequest, RestxRequestMatch match, Map input) throws IOException {
                                input.put("size", input.size());
                                return Optional.of(input);
                            }
                        })
                        .build())
                .build())
                .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
        server.start();
        try {
            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/route1/xavier");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("{\"id\":\"xavier\"}");

            httpRequest = HttpRequest.put(server.baseUrl() + "/api/route4").send("{\"test\":\"val1\"}");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("{\"test\":\"val1\",\"size\":1}");
        } finally {
            server.stop();
        }
    }

    @Test
    public void should_handle_fs_route() throws Exception {
        SimpleWebServer server = SimpleWebServer.builder().setRouter(StdRestxMainRouter.builder()
                .addRouter(FSRouter.mount("src/test/resources").readonly().on("/test"))
                .build())
                .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
        server.start();
        try {
            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/test.txt");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.contentType()).isEqualTo("text/plain; charset=UTF-8");
            assertThat(httpRequest.body().trim()).isEqualTo("hello");

            httpRequest = HttpRequest.put(server.baseUrl() + "/api/test/test.txt").send("bonjour");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
        } finally {
            server.stop();
        }
    }

    @Test
    public void should_handle_fs_route_dir_listing() throws Exception {
        SimpleWebServer server = SimpleWebServer.builder().setRouter(StdRestxMainRouter.builder()
                .addRouter(FSRouter.mount("src/test/resources")
                        .allowDirectoryListing().readonly().on("/test"))
                .build())
                .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
        server.start();
        try {
            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(replaceNewLine(httpRequest.body().trim())).isEqualTo("[\n" +
                    "\"test.txt\"\n" +
                    "]");
        } finally {
            server.stop();
        }
    }

    @Test
    public void should_handle_fs_route_write_file() throws Exception {
        if ("true".equals(System.getenv("DRONE"))) {
            // writing to FS causes problem on drone
            return;
        }
        File tmp = Files.newTemporaryFolder();
        SimpleWebServer server = SimpleWebServer.builder().setRouter(StdRestxMainRouter.builder()
                .addRouter(FSRouter.mount(tmp.getPath())
                        .allowDirectoryListing().on("/test"))
                .build())
                .setRouterPath("/api").setPort(WebServers.findAvailablePort()).build();
        server.start();
        try {
            HttpRequest httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(replaceNewLine(httpRequest.body().trim())).isEqualTo("[\n]");

            httpRequest = HttpRequest.put(server.baseUrl() + "/api/test/test.txt").send("bonjour");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.CREATED.getCode());

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/test.txt");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("bonjour");

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(replaceNewLine(httpRequest.body().trim())).isEqualTo("[\n" +
                    "\"test.txt\"\n" +
                    "]");

            httpRequest = HttpRequest.put(server.baseUrl() + "/api/test/test.txt").send("hello");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.ACCEPTED.getCode());

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/test.txt");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("hello");

            httpRequest = HttpRequest.put(server.baseUrl() + "/api/test/test2.txt").send("bonjour");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.CREATED.getCode());

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(replaceNewLine(httpRequest.body().trim())).isIn("[\n" +
                    "\"test.txt\",\n" +
                    "\"test2.txt\"\n" +
                    "]", "[\n" +
                    "\"test2.txt\",\n" +
                    "\"test.txt\"\n" +
                    "]");

            httpRequest = HttpRequest.delete(server.baseUrl() + "/api/test/test.txt");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.NO_CONTENT.getCode());
            httpRequest = HttpRequest.delete(server.baseUrl() + "/api/test/test2.txt");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.NO_CONTENT.getCode());

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(replaceNewLine(httpRequest.body().trim())).isEqualTo("[\n]");

            httpRequest = HttpRequest.put(server.baseUrl() + "/api/test/dir/").send("[]");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.CREATED.getCode());

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(replaceNewLine(httpRequest.body().trim())).isEqualTo("[\n" +
                    "\"dir/\"\n" +
                    "]");

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/dir/");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(replaceNewLine(httpRequest.body().trim())).isEqualTo("[\n]");


            httpRequest = HttpRequest.put(server.baseUrl() + "/api/test/dir/test.txt").send("bonjour");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.CREATED.getCode());

            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/dir/test.txt");
            assertThat(httpRequest.code()).isEqualTo(200);
            assertThat(httpRequest.body().trim()).isEqualTo("bonjour");
            /**
             * the delete is done after the addition here because windows 
             * Files.delete leave the folder in an unstable condition.
             */

            httpRequest = HttpRequest.delete(server.baseUrl() + "/api/test/dir/test.txt");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.NO_CONTENT.getCode());
            
            httpRequest = HttpRequest.delete(server.baseUrl() + "/api/test/dir/");
            assertThat(httpRequest.code()).isEqualTo(HttpStatus.NO_CONTENT.getCode());
//
//            httpRequest = HttpRequest.get(server.baseUrl() + "/api/test/");
//            assertThat(httpRequest.code()).isEqualTo(200);
//            assertThat(replaceNewLine(httpRequest.body().trim())).isEqualTo("[\n]");
        } finally {
            server.stop();
        }
    }
    
    public static String replaceNewLine(String string) {
    	return string.replaceAll("\r", "");
    }

}
