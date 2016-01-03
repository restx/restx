package restx.jackson;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import restx.factory.Factory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author fcamblor
 */
public class JacksonObjectReaderTightCouplingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldJacksonReadErrorMessageNotChange() throws IOException {
        ObjectReader reader = Factory.builder().addFromServiceLoader().build().getComponent(ObjectReader.class);
        File emptyFile = folder.newFile("empty-file");

        // In JsonEntityRequestBodyReader.readNullableValue() impl, we're tight coupling with Jackson's JsonMappingException
        // error message .. this test is intending to detect such potential API change in future versions of Jackson
        Object value = JsonEntityRequestBodyReader.readNullableValue(reader, Files.newInputStream(emptyFile.toPath()));
        assertThat(value, is(nullValue()));
    }
}
