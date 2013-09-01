package restx.specs;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.factory.Factory;
import restx.security.RestxSessionFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: xavierhanin
 * Date: 3/17/13
 * Time: 5:05 PM
 */
public class RestxSpecRecorder {
    private static final ThreadLocal<RestxSpecRecorder> current = new ThreadLocal<>();

    public static <T> T doWithRecorder(RestxSpecRecorder recorder, Callable<T> runnable) throws Exception {
        current.set(recorder);
        try {
            return runnable.call();
        } finally {
            current.remove();
        }
    }

    public static Optional<RestxSpecRecorder> current() {
        return Optional.fromNullable(current.get());
    }

    private final List<RecordedSpec> recordedSpecs = new CopyOnWriteArrayList<>();

    private final Set<GivenRecorder> recorders;
    private final RestxSessionFilter sessionFilter;

    public RestxSpecRecorder() {
        this(Factory.builder()
                .addLocalMachines(Factory.LocalMachines.threadLocal())
                .addLocalMachines(Factory.LocalMachines.contextLocal(RestxSpec.class.getSimpleName()))
                .addLocalMachines(Factory.LocalMachines.contextLocal(RestxSpecRecorder.class.getSimpleName()))
                .addFromServiceLoader()
                .build());
    }

    public RestxSpecRecorder(Factory factory) {
        this(factory.queryByClass(GivenRecorder.class).findAsComponents(),
                factory.queryByClass(RestxSessionFilter.class).mandatory().findOne().get().getComponent());
    }

    public RestxSpecRecorder(Set<GivenRecorder> recorders, RestxSessionFilter sessionFilter) {
        this.recorders = recorders;
        this.sessionFilter = sessionFilter;
    }

    public void install() {
        for (GivenRecorder recorder : recorders) {
            recorder.installRecording();
        }
    }


    /**
     * Start recording a request and response.
     * <p>
     * Make sure to use the recording request and response provided by the returned recorder in following
     * request handling.
     * </p>
     *
     * @param restxRequest  the request to record
     * @param restxResponse the response to record
     * @return a recorder tape
     * @throws IOException
     */
    public RestxSpecTape record(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
        return new RestxSpecTape(restxRequest, restxResponse, recorders, sessionFilter).doRecord();
    }

    public RecordedSpec stop(RestxSpecTape tape) {
        RecordedSpec recordedSpec = tape.close();
        recordedSpecs.add(recordedSpec);
        return recordedSpec;
    }

    public List<RecordedSpec> getRecordedSpecs() {
        return recordedSpecs;
    }

    public static interface GivenRecorder {
        void installRecording();
        AutoCloseable recordIn(Map<String, Given> givens);
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
