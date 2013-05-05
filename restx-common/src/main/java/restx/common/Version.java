package restx.common;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: xavierhanin
 * Date: 5/5/13
 * Time: 7:40 AM
 */
public class Version {
    public static String getVersion(String groupId, String module) {
        try (InputStream stream = Version.class.getResourceAsStream(
                "/META-INF/maven/" + groupId + "/" + module + "/pom.properties")) {

            if (stream == null) {
                return "DEV-" + DateTime.now().toString();
            }

            Properties properties = new Properties();
            properties.load(stream);
            return properties.getProperty("version");
        } catch (IOException e) {
            return "DEV-" + DateTime.now().toString();
        }
    }
}
