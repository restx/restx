package restx.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 2/2/14
 * Time: 11:07
 */
public class MessageParamsTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_serialize() throws Exception {
        String s = objectMapper.writer().writeValueAsString(MessageParams.of("key1", "value1"));
        assertThat(s).isEqualTo("{\"key1\":\"value1\"}");
    }

    @Test
    public void should_serialize_deserialize() throws Exception {
        assertSerializeDeserialize(MessageParams.empty());
        assertSerializeDeserialize(MessageParams.of("key1", "value1"));
    }

    protected void assertSerializeDeserialize(MessageParams params) throws java.io.IOException {
        String s = objectMapper.writer().writeValueAsString(params);
        MessageParams messageParams = objectMapper.readValue(s, MessageParams.class);
        assertThat(messageParams.toMap()).isEqualTo(params.toMap());
    }
}
