package restx.specs;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.jongo.MongoCollection;
import org.jongo.ResultHandler;
import restx.*;
import restx.factory.Factory;
import restx.factory.FactoryMachineWrapper;
import restx.factory.NamedComponent;
import restx.jongo.JongoCollection;
import restx.jongo.StdJongoCollection;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:05 PM
 */
public class SpecRecorder {
    public static final String RECORDING = "recording";
    private static final ReentrantLock lock = new ReentrantLock();
    private static final ThreadLocal<SpecRecorder> specRecorder = new ThreadLocal<>();
    private static final AtomicInteger specId = new AtomicInteger();
    static final List<RecordedSpec> specs = new CopyOnWriteArrayList<>();

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
        try {
            lock.tryLock(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("in record mode only one request at a time can be processed, " +
                    "another request is currently being processed which is taking too much time");
        }
        SpecRecorder recorder = new SpecRecorder(restxRequest, restxResponse);
        specRecorder.set(recorder);
        return recorder.doRecord();
    }


    private final RestxRequest restxRequest;
    private final RestxResponse restxResponse;

    private final RecordedSpec recordedSpec = new RecordedSpec();
    private final Map<String, RestxSpec.Given> givens = Maps.newLinkedHashMap();

    private RestxRequest recordingRequest;
    private RestxResponse recordingResponse;

    public SpecRecorder(RestxRequest restxRequest, RestxResponse restxResponse) {
        this.restxRequest = restxRequest;
        this.restxResponse = restxResponse;
    }

    public void close() {
        if (specRecorder.get() == SpecRecorder.this) {
            specRecorder.remove();
        }
        DateTimeUtils.setCurrentMillisSystem();
        lock.unlock();
    }

    public SpecRecorder doRecord() throws IOException {
        DateTime now = DateTime.now();
        givens.put(RestxSpec.GivenTime.class.getSimpleName() + "/now", new RestxSpec.GivenTime(now));
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
        recordedSpec.setRecordTime(now);
        Stopwatch stopwatch = new Stopwatch().start();
        System.out.print("RECORDING REQUEST...");
        final String method = restxRequest.getHttpMethod();
        final String path = restxRequest.getRestxUri().substring(1); // remove leading slash
        final Map<String,String> cookies = restxRequest.getCookiesMap();
        final byte[] requestBody = ByteStreams.toByteArray(restxRequest.getContentStream());
        System.out.println(" >> recorded request " + method + " " + path + " (" + requestBody.length + " bytes) -- " + stopwatch.stop());
        recordedSpec.setCapturedRequestSize(requestBody.length);

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
            private PrintWriter writer;
            private OutputStream realOS;
            public int status = 200;

            @Override
            public PrintWriter getWriter() throws IOException {
                if (writer == null) {
                    System.out.print("RECORDING RESPONSE...");
                    stopwatch.start();
                    realWriter = super.getWriter();
                    writer = new PrintWriter(baos = new ByteArrayOutputStream());
                }

                return writer;
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                System.out.print("RECORDING RESPONSE...");
                stopwatch.start();
                realOS = super.getOutputStream();
                return baos = new ByteArrayOutputStream();
            }

            @Override
            public void setStatus(int i) {
                super.setStatus(i);
                status = i;
            }

            @Override
            public void close() throws Exception {
                System.out.println(" >> recorded response (" + baos.size() + " bytes) -- " + stopwatch.stop());
                if (realWriter != null) {
                    CharStreams.copy(CharStreams.asCharSource(baos.toString("UTF-8")).openStream(), realWriter);
                } else if (realOS != null) {
                    ByteStreams.copy(ByteStreams.asByteSource(baos.toByteArray()).openStream(), realOS);
                }
                super.close();

                int id = specId.incrementAndGet();
                RestxSpec restxSpec = new RestxSpec(
                        String.format("%03d %s", id, path),
                        ImmutableList.copyOf(givens.values()), ImmutableList.<RestxSpec.When>of(
                        new RestxSpec.WhenHttpRequest(method, path, ImmutableMap.copyOf(cookies), new String(requestBody, Charset.forName("UTF-8")),
                                new RestxSpec.ThenHttpResponse(status, baos.toString("UTF-8")))));
                System.out.println("-----------------  RESTX SPEC  ---------------- \n"
                        + restxSpec + "\n"
                        + "------------------------------------------------"
                );
                specs.add(recordedSpec.setId(id).setSpec(restxSpec).setMethod(method).setPath(path)
                        .setDuration(new Duration(recordedSpec.getRecordTime(),
                                new DateTime(System.currentTimeMillis()))) // we don't use DateTime.now for that, time is still frozen
                        .setCapturedResponseSize(baos.size()));
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
                recordedSpec.capturedItems++;
                return JSON.serialize(result);
            }
        });

        givens.put(key, new RestxSpec.GivenCollection(mongoCollection.getName(), "", "       " + Joiner.on("\n       ").join(items), ImmutableList.<String>of()));
        System.out.println(" >> recorded " + mongoCollection.getName() + " -- " + stopwatch.toString());
    }

    public static class RecordedSpec {
        private RestxSpec spec;
        private DateTime recordTime;
        private Duration duration;
        private int capturedItems;
        private int capturedRequestSize;
        private int capturedResponseSize;
        private int id;
        private String path;
        private String method;

        public RecordedSpec() {
        }

        public RecordedSpec setSpec(final RestxSpec spec) {
            this.spec = spec;
            return this;
        }

        public RecordedSpec setRecordTime(final DateTime recordTime) {
            this.recordTime = recordTime;
            return this;
        }

        public RecordedSpec setDuration(final Duration duration) {
            this.duration = duration;
            return this;
        }

        public RecordedSpec setCapturedItems(final int capturedItems) {
            this.capturedItems = capturedItems;
            return this;
        }

        public RecordedSpec setCapturedRequestSize(final int capturedRequestSize) {
            this.capturedRequestSize = capturedRequestSize;
            return this;
        }

        public RecordedSpec setCapturedResponseSize(final int capturedResponseSize) {
            this.capturedResponseSize = capturedResponseSize;
            return this;
        }

        public RestxSpec getSpec() {
            return spec;
        }

        public DateTime getRecordTime() {
            return recordTime;
        }

        public Duration getDuration() {
            return duration;
        }

        public int getCapturedItems() {
            return capturedItems;
        }

        public int getCapturedRequestSize() {
            return capturedRequestSize;
        }

        public int getCapturedResponseSize() {
            return capturedResponseSize;
        }

        public int getId() {
            return id;
        }

        public RecordedSpec setId(final int id) {
            this.id = id;
            return this;
        }

        public String getPath() {
            return path;
        }

        public String getMethod() {
            return method;
        }

        public RecordedSpec setPath(final String path) {
            this.path = path;
            return this;
        }

        public RecordedSpec setMethod(final String method) {
            this.method = method;
            return this;
        }

    }
}
