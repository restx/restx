package samplest.core;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import restx.RestxContext;
import restx.tests.RestxServerRule;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 24/12/13
 * Time: 14:16
 */
public class PerRequestRecordingTest {
    @ClassRule
    public static RestxServerRule server = new RestxServerRule();
    private File dir;

    @Before public void setup() throws IOException {
        dir = Files.newTemporaryFolder();
    }

    @After public void teardown() throws IOException {
        Files.delete(dir);
    }

    @Test
    public void should_record_with_uuids() throws Exception {
        HttpRequest httpRequest = server.client().authenticatedAs("admin")
                .GET("/api/uuids/random")
                .header("RestxMode", RestxContext.Modes.RECORDING)
                .header("RestxRecordPath", dir.getAbsolutePath())
                ;

        assertThat(httpRequest.code()).isEqualTo(200);
        assertThat(httpRequest.body()).isNotEmpty();

        File record = new File(dir, "001_GET_uuids_random.spec.yaml");
        waitForFileExists(record);


        assertThat(Files.contentOf(record, Charsets.UTF_8))
                .startsWith("title: 001 GET uuids/random")
                .contains("wts:\n" +
                        "  - when: |\n" +
                        "       GET uuids/random\n")
                .contains("- time: ")
                .contains("- uuids: ")
        ;
    }

    protected void waitForFileExists(File record) throws InterruptedException {
        // recorded spec is saved asynchronously, let's wait for it to be saved to disk
        int c = 50;
        while (!record.exists() && c >= 0) {
            Thread.sleep(50);
            c--;
        }
    }
}
