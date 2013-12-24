package restx.specs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import restx.RestxRequest;
import restx.RestxRequestWrapper;
import restx.RestxResponse;
import restx.RestxResponseWrapper;
import restx.http.HttpStatus;
import restx.security.RestxSessionCookieFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:05 PM
 */
public class RestxSpecTape {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final AtomicInteger specId = new AtomicInteger();

    private static final ThreadLocal<RestxSpecTape> specTape = new ThreadLocal<>();

    private final RestxRequest restxRequest;
    private final RestxResponse restxResponse;

    private final RestxSpecRecorder.RecordedSpec recordedSpec = new RestxSpecRecorder.RecordedSpec();
    private final Map<String, Given> givens = Maps.newLinkedHashMap();
    private final Set<RestxSpecRecorder.GivenRecorder> recorders;
    private final Set<AutoCloseable> givenTapes = Sets.newLinkedHashSet();
    private final RestxSessionCookieFilter sessionFilter;
    private final RestxSpec.Storage storage;

    private RestxRequest recordingRequest;
    private RestxResponse recordingResponse;
    private int id;

    RestxSpecTape(RestxRequest restxRequest, RestxResponse restxResponse,
                  Set<RestxSpecRecorder.GivenRecorder> recorders, RestxSessionCookieFilter sessionFilter,
                  RestxSpec.StorageSettings storageSettings) {
        this.storage = RestxSpec.Storage.with(storageSettings);
        try {
            lock.tryLock(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("in record mode only one request at a time can be processed, " +
                    "another request is currently being processed which is taking too much time");
        }

        this.recorders = recorders;
        this.restxRequest = restxRequest;
        this.restxResponse = restxResponse;
        this.sessionFilter = sessionFilter;
    }

    public RestxSpecRecorder.RecordedSpec close() {
        if (specTape.get() == RestxSpecTape.this) {
            specTape.remove();
        }

        for (AutoCloseable givenTape : givenTapes) {
            try {
                givenTape.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        DateTimeUtils.setCurrentMillisSystem();
        lock.unlock();

        return recordedSpec;
    }

    public RestxSpecTape doRecord(final Optional<String> recordPath,
                                  final Optional<String> recordTitle) throws IOException {
        specTape.set(this);

        for (RestxSpecRecorder.GivenRecorder recorder : recorders) {
            givenTapes.add(recorder.recordIn(givens));
        }

        DateTime now = DateTime.now();
        givens.put(GivenTime.class.getSimpleName() + "/now", new GivenTime(now));
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
        recordedSpec.setRecordTime(now);
        Stopwatch stopwatch = new Stopwatch().start();
        System.out.print("RECORDING REQUEST...");
        final String method = restxRequest.getHttpMethod();
        final String path = restxRequest.getRestxUri().substring(1); // remove leading slash
        final ImmutableMap<String, String> cookies =
                sessionFilter.toCookiesMap(sessionFilter.buildContextFromRequest(restxRequest));
        final byte[] requestBody = ByteStreams.toByteArray(restxRequest.getContentStream());
        System.out.println(" >> recorded request " + method + " " + path + " (" + requestBody.length + " bytes) -- " + stopwatch.stop());
        recordedSpec.setCapturedRequestSize(requestBody.length);

        id = specId.incrementAndGet();
        final String title = recordTitle.or(buildTitle(id, method, path));
        final String specPath = storage.buildPath(recordPath, title);

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
            public HttpStatus status = HttpStatus.OK;

            @Override
            public PrintWriter getWriter() throws IOException {
                if (writer == null) {
                    if (recordPath.isPresent()) {
                        super.setHeader("RestxSpecPath", specPath);
                    }
                    System.out.print("RECORDING RESPONSE...");
                    stopwatch.start();
                    realWriter = super.getWriter();
                    writer = new PrintWriter(baos = new ByteArrayOutputStream());
                }

                return writer;
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                if (recordPath.isPresent()) {
                    super.setHeader("RestxSpecPath", specPath);
                }
                System.out.print("RECORDING RESPONSE...");
                stopwatch.start();
                realOS = super.getOutputStream();
                return baos = new ByteArrayOutputStream();
            }

            @Override
            public HttpStatus getStatus() {
                return status;
            }

            @Override
            public RestxResponse setStatus(HttpStatus i) {
                super.setStatus(i);
                status = i;
                return null;
            }

            @Override
            public void close() throws Exception {
                System.out.println(" >> recorded response (" + baos.size() + " bytes) -- " + stopwatch.stop());
                if (realWriter != null) {
                    CharStreams.copy(CharStreams.asCharSource(baos.toString(Charsets.UTF_8.name())).openStream(), realWriter);
                } else if (realOS != null) {
                    ByteStreams.copy(ByteStreams.asByteSource(baos.toByteArray()).openStream(), realOS);
                }
                super.close();

                RestxSpec restxSpec = new RestxSpec(
                        specPath, title,
                        ImmutableList.copyOf(givens.values()), ImmutableList.<When>of(
                        new WhenHttpRequest(method, path, cookies, new String(requestBody, Charsets.UTF_8),
                                new ThenHttpResponse(status.getCode(), baos.toString(Charsets.UTF_8.name())))));
                System.out.println("-----------------  RESTX SPEC  ---------------- \n"
                        + restxSpec + "\n"
                        + "------------------------------------------------"
                );
                recordedSpec.setId(id).setSpec(restxSpec).setMethod(method).setPath(path)
                        .setDuration(new Duration(recordedSpec.getRecordTime(),
                                new DateTime(System.currentTimeMillis()))) // we don't use DateTime.now for that, time is still frozen
                        .setCapturedResponseSize(baos.size());
            }
        };
        return this;
    }

    private String buildTitle(int id, String method, String path) {
        int endIndex = path.indexOf('?');
        endIndex = endIndex == -1 ? path.length() : endIndex;
        return String.format("%03d %s %s", id, method, path.substring(0, endIndex));
    }

    public RestxRequest getRecordingRequest() {
        return recordingRequest;
    }

    public RestxResponse getRecordingResponse() {
        return recordingResponse;
    }

}
