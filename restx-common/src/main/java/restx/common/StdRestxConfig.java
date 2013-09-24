package restx.common;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A standard implementation of RestxConfig.
 *
 * You can build it either from an iterable of ConfigElement, or by parsing a stream taking the form of a
 * properties file, where docs can be inserted using lines starting with #.
 */
public class StdRestxConfig implements RestxConfig {
    public static RestxConfig parse(String origin, InputSupplier<InputStreamReader> readerSupplier) throws IOException {
        List<ConfigElement> elements = new ArrayList<>();
        StringBuilder doc = new StringBuilder();
        int lineCount = 0;
        for (String line : CharStreams.readLines(readerSupplier)) {
            lineCount++;
            if (line.startsWith("#")) {
                doc.append(line.substring(1).trim()).append("\n");
            } else if (!line.trim().isEmpty()) {
                int index = line.indexOf('=');
                if (index == -1) {
                    throw new IOException("invalid config " + origin + " at line " + lineCount + ":" +
                            " line does not contain the equals sign '='");
                }
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();

                elements.add(ConfigElement.of(origin, doc.toString().trim(), key, value));
                doc.setLength(0);
            }
        }

        return new StdRestxConfig(elements);
    }

    public static RestxConfig of(Iterable<ConfigElement> configElements) {
        return new StdRestxConfig(configElements);
    }

    private final ImmutableMap<String, ConfigElement> elements;

    private StdRestxConfig(Iterable<ConfigElement> elements) {
        Map<String, ConfigElement> m = new LinkedHashMap<>();
        for (ConfigElement element : elements) {
            if (!m.containsKey(element.getKey())) {
                m.put(element.getKey(), element);
            }
        }
        this.elements = ImmutableMap.copyOf(m);
    }

    @Override
    public Iterable<ConfigElement> elements() {
        return elements.values();
    }

    @Override
    public Optional<ConfigElement> getElement(String elementKey) {
        return Optional.fromNullable(elements.get(elementKey));
    }

    @Override
    public Optional<String> getString(String elementKey) {
        ConfigElement element = elements.get(elementKey);
        if (element == null) {
            return Optional.absent();
        }
        return Optional.of(element.getValue());
    }

    @Override
    public Optional<Integer> getInt(String elementKey) {
        ConfigElement element = elements.get(elementKey);
        if (element == null) {
            return Optional.absent();
        }
        try {
            return Optional.of(Integer.parseInt(element.getValue()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("can't access " + element +
                    " as int" +
                    " (parse exception " + e.getMessage() + ")");
        }
    }
}
