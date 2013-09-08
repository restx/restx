package restx.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxContext;
import restx.classloader.ClasspathResourceEvent;
import restx.classloader.CompilationFinishedEvent;
import restx.common.UUIDGenerator;
import restx.exceptions.ErrorCode;
import restx.exceptions.ErrorField;
import restx.exceptions.RestxError;
import restx.factory.Factory;
import restx.factory.NamedComponent;
import restx.factory.SingletonFactoryMachine;
import restx.server.WebServer;
import restx.server.WebServerSupplier;
import restx.specs.HotReloadRestxSpecRepository;
import restx.specs.RestxSpecLoader;
import restx.specs.RestxSpecRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: xavierhanin
 * Date: 7/30/13
 * Time: 9:23 PM
 */
public class RestxSpecTestServer {

    public static Factory defaultFactory() {
        return RestxSpecRunner.defaultFactory();
    }

    /**
     * A shortcut for new RestxSpecRule("/api", 8076, queryByClass(WebServerSupplier.class), defaultFactory())
     */
    public static RestxSpecTestServer newInstance() {
        Factory f = defaultFactory();
        return new RestxSpecTestServer("/api", 8076,
                f.queryByClass(WebServerSupplier.class).findOne().get().getComponent(), f);
    }

    /**
     * A shortcut for new RestxSpecRule("/api", 8076, webServerSupplier, defaultFactory())
     */
    public static RestxSpecTestServer newInstance(WebServerSupplier webServerSupplier) {
        return new RestxSpecTestServer("/api", 8076, webServerSupplier, defaultFactory());
    }

    private final String routerPath;
    private final int port;
    private final WebServerSupplier webServerSupplier;
    private final Factory factory;

    public static class RunningServer {
        private static final Logger logger = LoggerFactory.getLogger(RunningServer.class);

        private final WebServer server;
        private final RestxSpecRunner runner;
        private final RestxSpecRepository repository;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Path storeLocation = Paths.get(System.getProperty("restx.targetTestsRoot", "tmp/tests"));
        private final ObjectMapper objectMapper;
        private final Map<String, TestResultSummary> lastResults;

        public RunningServer(WebServer server, RestxSpecRunner runner, RestxSpecRepository repository) {
            this.server = server;
            this.runner = runner;
            this.repository = repository;

            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JodaModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            lastResults = loadLastResults();

            Factory.LocalMachines.contextLocal(server.getServerId()).addMachine(
                    new SingletonFactoryMachine<>(0, NamedComponent.of(RunningServer.class, "RunningServer", this)));
        }

        public void stop() throws Exception {
            runner.dispose();
            server.stop();
        }


        public TestRequest submitTestRequest(TestRequest testRequest) {
            if (testRequest.getTest().startsWith("specs")) {
                final String requestKey = UUIDGenerator.DEFAULT.doGenerate();
                logger.info("queuing test request {}", testRequest);
                testRequest.setKey(requestKey);
                testRequest.setRequestTime(DateTime.now());
                testRequest.setStatus(TestRequest.Status.QUEUED);
                store(testRequest);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        Optional<TestRequest> requestOptional = getRequestByKey(requestKey);
                        if (!requestOptional.isPresent()) {
                            logger.warn("test request not found when trying to execute it: {}", requestKey);
                            return;
                        }
                        TestRequest testRequest = requestOptional.get();
                        logger.info("running test request {}", testRequest);
                        testRequest.setStatus(TestRequest.Status.RUNNING);
                        store(testRequest);

                        List<String> resultKeys = new ArrayList<>();
                        try {
                            String spec = testRequest.getTest();
                            if (spec.endsWith("*")) {
                                // clear last results when we run all tests
                                // this is currently the only way to cleanup last results
                                if (spec.equals("specs/*")) {
                                    synchronized (lastResults) {
                                        lastResults.clear();
                                    }
                                }

                                String prefix = spec.substring(0, spec.length() - 1);
                                for (String s : repository.findAll()) {
                                    if (s.startsWith(prefix)) {
                                        runSpecTest(s, resultKeys);
                                    }
                                }
                            } else {
                                runSpecTest(spec, resultKeys);
                            }
                        } finally {
                            testRequest.setStatus(TestRequest.Status.DONE);
                            testRequest.setTestResultKey(Joiner.on(",").join(resultKeys));
                            store(testRequest);
                        }
                    }
                });

                return testRequest;
            } else {
                throw RestxError.on(Rules.InvalidTest.class)
                        .set(Rules.InvalidTest.TEST, testRequest.getTest())
                        .set(Rules.InvalidTest.DESCRIPTION, "can only run spec test, test field must start with 'specs'")
                        .raise();
            }
        }

        private void runSpecTest(String spec, List<String> resultKeys) {
            logger.info("running spec test {}", spec);
            PrintStream out = System.out;
            PrintStream err = System.err;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outStream));
            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errStream));
            TestResultSummary.Status status = TestResultSummary.Status.ERROR;
            long start = System.currentTimeMillis();
            try {
                runner.runTest(repository.findSpecById(spec).get());
                status = TestResultSummary.Status.SUCCESS;
            } catch (AssertionError e) {
                status = TestResultSummary.Status.FAILURE;
                System.err.println(e.getMessage());
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            } finally {
                System.setOut(out);
                System.setErr(err);

                TestResult result = new TestResult()
                        .setSummary(new TestResultSummary()
                                .setKey(UUIDGenerator.DEFAULT.doGenerate())
                                .setName(spec)
                                .setStatus(status)
                                .setTestDuration(System.currentTimeMillis() - start)
                                .setTestTime(new DateTime(start))
                        )
                        .setStdOut(new String(outStream.toByteArray()))
                        .setStdErr(new String(errStream.toByteArray()))
                        ;
                store(result);
                resultKeys.add(result.getSummary().getKey());
            }
        }

        private void store(TestRequest testRequest) {
            try {
                File file = testRequestFile(testRequest.getKey());
                file.getParentFile().mkdirs();
                objectMapper.writeValue(file, testRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Optional<TestRequest> getRequestByKey(String key) {
            File file = testRequestFile(key);
            if (!file.exists()) {
                return Optional.absent();
            }

            try {
                TestRequest testRequest = objectMapper.readValue(file, TestRequest.class);
                return Optional.of(testRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private File testRequestFile(String key) {
            return storeLocation.resolve(
                    "requests/" + key + ".json").toFile();
        }

        private Map<String,TestResultSummary> loadLastResults() {
            Map<String, TestResultSummary> results = new HashMap<>();

            File src = lastResultSummariesFile();
            if (!src.exists()) {
                return results;
            }

            try {
                Collection<TestResultSummary> summaries = objectMapper.readValue(src,
                        new TypeReference<Collection<TestResultSummary>>() { });

                for (TestResultSummary summary : summaries) {
                    results.put(summary.getName(), summary);
                }
            } catch (IOException e) {
                logger.error("error reading last result summaries file - will start with empty data", e);
                results.clear();
            }

            return results;
        }

        private void store(TestResult result) {
            try {
                String key = result.getSummary().getKey();
                File resultFile = testResultSummaryFile(key);
                resultFile.getParentFile().mkdirs();

                Files.write(result.getStdOut(), testResultStdOutFile(key), Charsets.UTF_8);
                Files.write(result.getStdErr(), testResultStdErrFile(key), Charsets.UTF_8);
                objectMapper.writeValue(resultFile, result.getSummary());

                synchronized (lastResults) {
                    lastResults.put(result.getSummary().getName(), result.getSummary());
                }
                objectMapper.writeValue(lastResultSummariesFile(), lastResults.values());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Optional<TestResult> getResultByKey(String key) {
            File file = testResultSummaryFile(key);
            if (!file.exists()) {
                return Optional.absent();
            }

            try {
                TestResult testResult = new TestResult()
                        .setSummary(objectMapper.readValue(file, TestResultSummary.class))
                        .setStdOut(Files.toString(testResultStdOutFile(key), Charsets.UTF_8))
                        .setStdErr(Files.toString(testResultStdErrFile(key), Charsets.UTF_8))
                        ;
                return Optional.of(testResult);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private File lastResultSummariesFile() {
            return storeLocation.resolve(
                    "results/last.summaries.json").toFile();
        }

        private File testResultStdOutFile(String key) {
            return storeLocation.resolve(
                    "results/" + key + ".stdout.txt").toFile();
        }

        private File testResultStdErrFile(String key) {
            return storeLocation.resolve(
                    "results/" + key + ".stderr.txt").toFile();
        }

        private File testResultSummaryFile(String key) {
            return storeLocation.resolve(
                    "results/" + key + ".summary.json").toFile();
        }

        public Iterable<TestResultSummary> findCurrentTestResults() {
            synchronized (lastResults) {
                return new ArrayList<>(lastResults.values());
            }
        }

        public static class Rules {
            @ErrorCode(code = "TEST-001", description = "invalid test")
            public static enum InvalidTest {
                @ErrorField("requested test") TEST,
                @ErrorField("description why the test is invalid") DESCRIPTION
            }
        }
    }

    /**
     * Constructs a new RestxSpecRule.
     *
     * @param routerPath the path at which restx router is mounted. eg '/api'
     * @param webServerSupplier a supplier of WebServer, you can use #jettyWebServerSupplier for jetty.
     * @param factory the restx Factory to use to find GivenSpecRuleSupplier s when executing the spec.
     *                This is not used for the server itself.
     */
    public RestxSpecTestServer(String routerPath, int port, WebServerSupplier webServerSupplier, Factory factory) {
        this.routerPath = routerPath;
        this.port = port;
        this.webServerSupplier = webServerSupplier;
        this.factory = factory;
    }

    public RunningServer start() throws Exception {
        System.setProperty("restx.mode", RestxContext.Modes.TEST);
        WebServer server = webServerSupplier.newWebServer(port);
        server.start();
        RestxSpecLoader specLoader = new RestxSpecLoader(factory);
        RestxSpecRunner runner = new RestxSpecRunner(specLoader, routerPath, server.getServerId(), server.baseUrl(), factory);
        RestxSpecRepository repository = new HotReloadRestxSpecRepository(specLoader);

        final RunningServer runningServer = new RunningServer(server, runner, repository);

        server.getEventBus().register(new Object() {
            @Subscribe
            public void onCompilationFinished(
                    CompilationFinishedEvent event) {
                runningServer.submitTestRequest(new TestRequest().setTest("specs/*"));
            }

            @Subscribe
            public void onResourceEvent(ClasspathResourceEvent event) {
                if (event.getResourcePath().startsWith("specs")) {
                    runningServer.submitTestRequest(new TestRequest().setTest(event.getResourcePath()));
                } else {
                    runningServer.submitTestRequest(new TestRequest().setTest("specs/*"));
                }
            }
        });

        return runningServer;
    }

}
