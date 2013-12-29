package restx.specs.mongo;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import restx.factory.Component;
import restx.specs.Given;
import restx.specs.RestxSpecLoader;

import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static restx.common.MorePreconditions.checkInstanceOf;

/**
* User: xavierhanin
* Date: 3/30/13
* Time: 7:01 PM
*/
@Named("collection") @Component
public final class GivenJongoCollectionLoader implements RestxSpecLoader.GivenLoader {
    @Override
    public Given load(Map given) {
        String path = given.containsKey("path") ? checkInstanceOf("path", given.get("path"), String.class) : "data://";
        String data;
        if (given.containsKey("data")) {
            data = checkInstanceOf("data", given.get("data"), String.class);
        } else if (given.containsKey("path")) {
            if (path.startsWith("/")) {
                try {
                    data = Resources.toString(Resources.getResource(path.substring(1)), Charsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalArgumentException("can't load referenced resource " + path + " for " + given, e);
                }
            } else {
                throw new IllegalArgumentException("only absolute resource paths are supported for collection data." +
                        " was: " + path + " in " + given);
            }
        } else {
            data = "";
        }
        List<String> sequence = Lists.newArrayList();
        if (given.containsKey("sequence")) {
            Object seq = given.get("sequence");
            if (seq instanceof String) {
                String s = (String) seq;
                Iterables.addAll(sequence, Splitter.on(",").omitEmptyStrings().trimResults().split(s));
            } else if (seq instanceof Iterable) {
                Iterables.addAll(sequence, asIterableString((Iterable) seq));
            } else {
                throw new IllegalArgumentException("unsupported type for sequence in " + given +
                        ": " + seq.getClass().getName() +
                        " must be either String or Iterable.");
            }
        }

        return new GivenJongoCollection(
                checkInstanceOf("collection", given.get("collection"), String.class),
                path,
                data,
                ImmutableList.copyOf(sequence));
    }

    @SuppressWarnings("unchecked")
    protected Iterable<? extends String> asIterableString(Iterable seq) {
        return (Iterable<? extends String>) seq;
    }
}
