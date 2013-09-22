package restx.shell;

import org.apache.ivy.Ivy;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.plugins.repository.TransferListener;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * User: xavierhanin
 * Date: 7/28/13
 * Time: 9:24 PM
 */
public class ShellIvy {
    private static final Logger logger = LoggerFactory.getLogger(ShellIvy.class);

    public static Ivy loadIvy(final RestxShell shell) {
        Ivy ivy = new Ivy();

        File settingsFile = shell.installLocation().resolve("ivysettings.xml").toFile();
        try {
            if (settingsFile.exists()) {
                logger.info("loading ivy settings from " + settingsFile.getAbsolutePath());
                ivy.configure(settingsFile);
            } else {
                URL settingsURL = ShellIvy.class.getResource("ivysettings.xml");
                logger.info("loading ivy settings from " + settingsURL);
                ivy.configure(settingsURL);
            }

            ivy.getEventManager().addTransferListener(new TransferListener() {
                public void transferProgress(TransferEvent evt) {
                    switch (evt.getEventType()) {
                        case TransferEvent.TRANSFER_STARTED:
                            try {
                                shell.println("downloading " + evt.getResource().getName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case TransferEvent.TRANSFER_PROGRESS:
                            try {
                                shell.updateProgress(evt.getResource().getName(), evt.getLength(), evt.getTotalLength());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case TransferEvent.TRANSFER_COMPLETED:
                            try {
                                shell.updateProgress(evt.getResource().getName(), evt.getTotalLength(), evt.getTotalLength());
                                shell.endProgress(evt.getName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        default:
                            break;
                    }
                }
            });

            ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG) {
                @Override
                public void log(String msg, int level) {
                    try {
                        if (level <= Message.MSG_ERR) {
                            logger.error(msg);
                            shell.printIn(msg, RestxShell.AnsiCodes.ANSI_RED);
                            shell.println("");
                        } else if (level <= Message.MSG_WARN) {
                            logger.warn(msg);
                            shell.printIn(msg, RestxShell.AnsiCodes.ANSI_YELLOW);
                            shell.println("");
                        } else if (level <= Message.MSG_INFO) {
                            logger.info(msg);
                            shell.println(msg);
                        } else {
                            logger.debug(msg);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void doProgress() {
                    // DO NOTHING, progress is reported thanks to TransferEvent handling
                }

                @Override
                public void doEndProgress(String msg) {
                    // DO NOTHING, progress is reported thanks to TransferEvent handling
                }
            });

            return ivy;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
