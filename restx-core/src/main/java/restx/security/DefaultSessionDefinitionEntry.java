package restx.security;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import restx.security.RestxSession.Definition.Entry;

/**
 * Date: 20/1/15
 * Time: 20:56
 */
public class DefaultSessionDefinitionEntry<T> implements Entry<T> {
    private final String sessionDefKey;
    private final Function<String, Optional<? extends T>> function;

    public DefaultSessionDefinitionEntry(Class<T> clazz, String sessionDefKey, Function<String, Optional<? extends T>> function) {
        this.sessionDefKey = sessionDefKey;
        this.function = function;
    }

    @Override
    public String getKey() {
        return sessionDefKey;
    }

    @Override
    public Optional<? extends T> getValueForId(String valueId) {
        return function.apply(valueId);
    }
}
