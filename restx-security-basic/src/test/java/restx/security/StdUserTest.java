package restx.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 14/12/13
 * Time: 17:00
 */
public class StdUserTest {
    @Test
    public void should_serialize_deserialize() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new GuavaModule());

        String admin = objectMapper.writer().writeValueAsString(
                new StdUser("admin", ImmutableSet.<String>of("restx-admin")));

        assertThat(admin).isEqualTo("{\"name\":\"admin\",\"roles\":[\"restx-admin\"]}");

        StdUser u = objectMapper.reader().withType(StdUser.class).readValue(admin);
        assertThat(u.getName()).isEqualTo("admin");
        assertThat(u.getPrincipalRoles()).isEqualTo(ImmutableSet.<String>of("restx-admin"));

    }
}
