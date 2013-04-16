package restx.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 4/16/13
 * Time: 11:55 PM
 */
public class RestxBuildHelper {
    // don't want to introduce a dependency just for that
    public static String toString(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static String expandProperties(Map<String, String> properties, String s) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            s = expandProperty(s, entry.getKey(), entry.getValue());
        }
        return s;
    }

    public static String expandProperty(String s, String key, String value) {
        return s.replace("${" + key + "}", value);
    }
}
