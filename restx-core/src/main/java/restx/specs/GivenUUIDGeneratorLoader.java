package restx.specs;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import restx.factory.Component;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static restx.common.MorePreconditions.checkContainsKey;

/**
 * @author fcamblor
 */
@Named("uuids") @Component
public class GivenUUIDGeneratorLoader implements RestxSpecLoader.GivenLoader {
    @Override
    public Given load(Map<String, ?> given) {
        List<String> uuids = newArrayList();
        Object data = checkContainsKey("given", given, "uuids");
        if (data instanceof String) {
            String s = (String) data;
            Iterables.addAll(uuids, Splitter.on(",").omitEmptyStrings().trimResults().split(s));
        } else if (data instanceof Iterable) {
            Iterables.addAll(uuids, asIterableString((Iterable) data));
        } else if (data != null) {
            throw new IllegalArgumentException("unsupported type for uuids data in " + given +
                    ": " + data.getClass().getName() +
                    " must be either String or Iterable.");
        }

        return new GivenUUIDGenerator(ImmutableList.copyOf(uuids));
    }

    @SuppressWarnings("unchecked")
    protected Iterable<? extends String> asIterableString(Iterable<?> data) {
        return (Iterable<? extends String>) data;
    }
}
