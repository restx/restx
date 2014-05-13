package restx.security;

import org.junit.Test;
import restx.factory.Factory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 13/5/14
 * Time: 20:55
 */
public class EmptySessionProviderTest {
    @Test
    public void should_provide_empty_session() throws Exception {
        RestxSession restxSession = Factory.getInstance().getComponent(EmptySessionProvider.class).get();
        assertThat(restxSession).isNotNull();
        assertThat(restxSession.getPrincipal().isPresent()).isFalse();
    }


}
