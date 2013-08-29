package restx.specs;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import restx.factory.Component;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static restx.common.MorePreconditions.checkContainsKey;
import static restx.common.MorePreconditions.checkInstanceOf;

/**
 * @author fcamblor
 */
@Named("uuidsFor") @Component
public class GivenUUIDGeneratorLoader implements RestxSpecLoader.GivenLoader {
    @Override
    public Given load(Map given) {
        String targetComponentName = checkInstanceOf("uuidsFor", given.get("uuidsFor"), String.class);

        List<String> uuids = newArrayList();
        Object data = checkContainsKey("given", given, "data");
        if (data instanceof String) {
            String s = (String) data;
            Iterables.addAll(uuids, Splitter.on(",").omitEmptyStrings().trimResults().split(s));
        } else if (data instanceof Iterable) {
            Iterables.addAll(uuids, (Iterable<? extends String>) data);
        } else {
            throw new IllegalArgumentException("unsupported type for uuids data in " + given +
                    ": " + data.getClass().getName() +
                    " must be either String or Iterable.");
        }

        return new GivenUUIDGenerator(targetComponentName, uuids);
    }
}
