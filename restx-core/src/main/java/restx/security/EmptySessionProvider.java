package restx.security;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.joda.time.Duration;
import restx.factory.Component;
import restx.security.RestxSession.Definition;

/**
 * A component able to provide an empty session.
 *
 * This is useful especially for testing, allowing to create an empty RestxSession
 * and then populate it.
 */
@Component
public class EmptySessionProvider implements Supplier<RestxSession> {
    private final Definition definition;

    public EmptySessionProvider(Definition definition) {
        this.definition = definition;
    }

    @Override
    public RestxSession get() {
        return new RestxSession(definition,
                ImmutableMap.<String, String>of(),
                Optional.<RestxPrincipal>absent(),
                Duration.ZERO);
    }
}
