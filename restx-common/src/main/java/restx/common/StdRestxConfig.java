package restx.common;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharSource;

/**
 * A standard implementation of RestxConfig.
 *
 * You can build it either from an iterable of ConfigElement, or by parsing a stream taking the form of a
 * properties file, where docs can be inserted using lines starting with #.
 */
public class StdRestxConfig implements RestxConfig {
	private static final Collection<String> TRUE_VALUES = asList("true", "yes", "on", "1", "y");

	public static RestxConfig parse(String origin, CharSource charSource) throws IOException {
		List<ConfigElement> elements = new ArrayList<>();
		StringBuilder doc = new StringBuilder();
		int lineCount = 0;
		String lkey = "";
		String lvalue = "";
		for (String line : charSource.readLines()) {
			lineCount++;
			if (line.startsWith("#")) {
				doc.append(line.substring(1).trim()).append("\n");
			} else if (!line.trim().isEmpty()) {
				int index = line.indexOf('=');

				if (line.contains("\\")) {
					lkey = line.contains("=") ? line.split("=")[0].trim() : lkey;
					if (line.endsWith("\\")) {
					lvalue += line.contains("=") ? line.split("=")[1].trim() : line.trim();
						continue;
					}	
				}
				else 
					if (index == -1) {
						if (!lvalue.endsWith("\\")) {
							throw new IOException("invalid config " + origin + " at line " + lineCount + ":" +
									" line does not contain the equals sign '='");
						}
					}

				String key;
				if (!lkey.isEmpty()) {
					key = lkey;
				}
				else {
					key = line.substring(0, index).trim();
				}
				
				String value = lvalue.trim() + line.substring(index + 1).trim();
				value = value.replace("\\", "");
				lvalue = "";
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
			ConfigElement curElement = m.get(element.getKey());
			if (curElement == null) {
				m.put(element.getKey(), element);
			} else {
				if (isNullOrEmpty(curElement.getDoc())
						&& !isNullOrEmpty(element.getDoc())) {
					m.put(element.getKey(), curElement.withDoc(element.getDoc()));
				}
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
		if (element == null || isNullOrEmpty(element.getValue())) {
			return Optional.absent();
		}
		return Optional.of(element.getValue());
	}

	@Override
	public Optional<Integer> getInt(String elementKey) {
		ConfigElement element = elements.get(elementKey);
		if (element == null || isNullOrEmpty(element.getValue())) {
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

	@Override
	public Optional<Long> getLong(String elementKey) {
		ConfigElement element = elements.get(elementKey);
		if (element == null || isNullOrEmpty(element.getValue())) {
			return Optional.absent();
		}
		try {
			return Optional.of(Long.parseLong(element.getValue()));
		} catch (NumberFormatException e) {
			throw new RuntimeException("can't access " + element +
					" as long" +
					" (parse exception " + e.getMessage() + ")");
		}
	}

	@Override
	public Optional<Boolean> getBoolean(String elementKey) {
		ConfigElement element = elements.get(elementKey);
		if (element == null || isNullOrEmpty(element.getValue())) {
			return Optional.absent();
		}
		return Optional.of(TRUE_VALUES.contains(element.getValue().toLowerCase(Locale.ENGLISH)));
	}
}
