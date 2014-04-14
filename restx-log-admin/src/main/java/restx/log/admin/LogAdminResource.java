package restx.log.admin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import restx.WebException;
import restx.annotations.GET;
import restx.annotations.PUT;
import restx.annotations.RestxResource;
import restx.common.MorePreconditions;
import restx.factory.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Date: 17/11/13
 * Time: 10:24
 */
@RestxResource(group = "restx-admin") @Component
public class LogAdminResource {
    public static class Logger {
        public String name;
        public String level;

        public Logger() {
        }

        public Logger(String name, String level) {
            this.name = name;
            this.level = level;
        }
    }

    @GET("/@/logs")
    public String getLogs() {
        // quick and dirty basic implementation to get logs from the default log file.
        // this doesn't scale at all, and is very limited
        try {
            File appLog = new File(System.getProperty("logs.base", "logs"), "app.log");
            String logs = Files.toString(
                    appLog, Charsets.UTF_8);
            // limit to around 30k
            int limit = 30000;
            if (logs.length() > limit) {
                int length = logs.length();
                logs = logs.substring(logs.length() - limit);
                logs = logs.substring(logs.indexOf('\n') + 1);
                logs = "[... " + ((length - logs.length()) / 1024)  + " kB truncated ...]\n" + logs;
            }
            logs = "LOGS FROM: " + appLog.getAbsolutePath() + "\n" +
                    "------------------------------------------------------------------------------------------------\n" +
                    logs;
            return logs;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @GET("/@/loggers")
    public Iterable<Logger> getLoggers() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (loggerFactory instanceof LoggerContext) {
            LoggerContext context = (LoggerContext) loggerFactory;

            List<Logger> loggers = new ArrayList<>();
            for (ch.qos.logback.classic.Logger l : context.getLoggerList()) {
                loggers.add(new Logger(l.getName(), l.getEffectiveLevel().toString()));
            }

            return loggers;
        } else {
            return Collections.emptyList();
        }
    }

    @PUT("/@/loggers/{name}")
    public Logger updateLogger(String name, Logger logger) {
        logger.name = name;

        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (loggerFactory instanceof LoggerContext) {
            LoggerContext context = (LoggerContext) loggerFactory;
            context.getLogger(name).setLevel(Level.valueOf(logger.level));
        }

        return logger;
    }
}
