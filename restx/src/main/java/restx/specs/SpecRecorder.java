package restx.specs;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.jongo.MongoCollection;
import org.jongo.ResultHandler;
import restx.RestxRequest;
import restx.RestxRequestWrapper;
import restx.RestxResponse;
import restx.RestxResponseWrapper;
import restx.factory.Factory;
import restx.factory.FactoryMachineWrapper;
import restx.factory.NamedComponent;
import restx.jongo.JongoCollection;
import restx.jongo.StdJongoCollection;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:05 PM
 */
public class SpecRecorder {
    public static final String RECORDING = "recording";
    private static final ThreadLocal<SpecRecorder> specRecorder = new ThreadLocal<>();

    public static void install() {
        Factory.LocalMachines.contextLocal(RECORDING).addMachine(
            FactoryMachineWrapper.from(new StdJongoCollection.JongoCollectionFactory())
                .withPriority(-10)
                .transformComponents(new Function<NamedComponent, NamedComponent>() {
                    @Override
                    public NamedComponent apply(final NamedComponent input) {
                        final JongoCollection collection = (JongoCollection) input.getComponent();
                        return new NamedComponent<>(input.getName(),
                                new JongoCollection() {
                                    @Override
                                    public String getName() {
                                        return collection.getName();
                                    }

                                    @Override
                                    public MongoCollection get() {
                                        MongoCollection mongoCollection = collection.get();
                                        if (specRecorder.get() != null) {
                                            specRecorder.get().recordCollection(mongoCollection);
                                        }
                                        return mongoCollection;
                                    }
                                });
                    }
                }).build());
    }


    /**
     * Start recording a request and response.
     * <p>
     * Make sure to use the recording request and response provided by the returned recorder in following
     * request handling.
     * </p>
     *
     * @param restxRequest the request to record
     * @param restxResponse the response to record
     * @return a recorder
     * @throws IOException
     */
    public static SpecRecorder record(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
        SpecRecorder recorder = new SpecRecorder(restxRequest, restxResponse);
        specRecorder.set(recorder);
        return recorder.doRecord();
    }


    private final RestxRequest restxRequest;
    private final RestxResponse restxResponse;

    private final Map<String, RestxSpec.Given> givens = Maps.newLinkedHashMap();

    private RestxRequest recordingRequest;
    private RestxResponse recordingResponse;

    public SpecRecorder(RestxRequest restxRequest, RestxResponse restxResponse) {
        this.restxRequest = restxRequest;
        this.restxResponse = restxResponse;
    }

    public SpecRecorder doRecord() throws IOException {
        Stopwatch stopwatch = new Stopwatch().start();
        System.out.print("RECORDING REQUEST...");
        final String method = restxRequest.getHttpMethod();
        final String path = restxRequest.getRestxUri();
        final byte[] requestBody = ByteStreams.toByteArray(restxRequest.getContentStream());
        System.out.println(" >> recorded request " + method + " " + path + " (" + requestBody.length + " bytes) -- " + stopwatch.stop());
        recordingRequest = new RestxRequestWrapper(restxRequest) {
            @Override
            public InputStream getContentStream() throws IOException {
                return new ByteArrayInputStream(requestBody);
            }
        };
        recordingResponse = new RestxResponseWrapper(restxResponse) {
            private Stopwatch stopwatch = new Stopwatch();
            private ByteArrayOutputStream baos;
            private PrintWriter realWriter;
            private OutputStream realOS;

            @Override
            public PrintWriter getWriter() throws IOException {
                System.out.print("RECORDING RESPONSE...");
                stopwatch.start();
                realWriter = super.getWriter();
                return new PrintWriter(baos = new ByteArrayOutputStream());
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                System.out.print("RECORDING RESPONSE...");
                stopwatch.start();
                realOS = super.getOutputStream();
                return baos = new ByteArrayOutputStream();
            }

            @Override
            public void close() throws Exception {
                System.out.println(" >> recorded response (" + baos.toByteArray().length + " bytes) -- " + stopwatch.stop());
                if (realWriter != null) {
                    CharStreams.copy(CharStreams.asCharSource(baos.toString("UTF-8")).openStream(), realWriter);
                } else if (realOS != null) {
                    ByteStreams.copy(ByteStreams.asByteSource(baos.toByteArray()).openStream(), realOS);
                }
                super.close();

                Appendable sb = new StringBuilder();
                sb.append("title: ").append(path).append("\n");
                if (!givens.isEmpty()) {
                    sb.append("given:\n");
                    for (RestxSpec.Given given : givens.values()) {
                        sb.append(given.toString());
                    }
                }
                sb.append("wts:\n");
                String body = new String(requestBody, Charset.forName("UTF-8"));
                if (Strings.isNullOrEmpty(body)) {
                    sb.append("  - when: ").append(method).append(" ").append(path).append("\n");
                } else {
                    sb.append("  - when: |\n")
                      .append("       ").append(method).append(" ").append(path).append("\n\n")
                      .append("       ").append(body).append("\n");
                }
                sb.append("    then: |\n")
                        .append("       ").append(baos.toString("UTF-8")).append("\n");

                System.out.println("-----------------  RESTX SPEC  ---------------- \n"
                        + sb + "\n"
                        + "------------------------------------------------"
                );

                if (specRecorder.get() == SpecRecorder.this) {
                    specRecorder.remove();
                }
            }
        };
        return this;
    }

    public RestxRequest getRecordingRequest() {
        return recordingRequest;
    }

    public RestxResponse getRecordingResponse() {
        return recordingResponse;
    }

    public void recordCollection(MongoCollection mongoCollection) {
        String key = RestxSpec.GivenCollection.class.getSimpleName() + "/" + mongoCollection.getName();
        if (givens.containsKey(key)) {
            return;
        }
        Stopwatch stopwatch = new Stopwatch().start();
        System.out.print("RECORDING " + mongoCollection.getName() + "...");
        Iterable<String> items = mongoCollection.find().map(new ResultHandler<String>() {
            @Override
            public String map(DBObject result) {
                return JSON.serialize(result);
            }
        });

        givens.put(key, new RestxSpec.GivenCollection(mongoCollection.getName(), "", "       " + Joiner.on("\n       ").join(items), ImmutableList.<String>of()));
        System.out.println(" >> recorded " + mongoCollection.getName() + " -- " + stopwatch.toString());
    }
}
