package restx.shell;

import org.apache.ivy.Ivy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;

/**
 * User: xavierhanin
 * Date: 7/28/13
 * Time: 9:24 PM
 */
public class ShellIvy {
    private static final Logger logger = LoggerFactory.getLogger(ShellIvy.class);

    public static Ivy loadIvy(Path shellInstallLocation) {
        Ivy ivy = new Ivy();

        File settingsFile = shellInstallLocation.resolve("ivysettings.xml").toFile();
        try {
            if (settingsFile.exists()) {
                logger.info("loading ivy settings from " + settingsFile.getAbsolutePath());
                ivy.configure(settingsFile);
            } else {
                URL settingsURL = ShellIvy.class.getResource("ivysettings.xml");
                logger.info("loading ivy settings from " + settingsURL);
                ivy.configure(settingsURL);
            }
            return ivy;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
