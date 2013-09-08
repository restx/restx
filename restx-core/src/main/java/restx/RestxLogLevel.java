package restx;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;

/**
 * A log verbosity level for restx request / response logging.
 */
public enum RestxLogLevel {
    VERBOSE {
        public void log(Logger logger, RestxRequest restxRequest, RestxResponse restxResponse, Stopwatch stopwatch) {
            logger.info("<< {}\n{}\n>> {} - {}", restxRequest, restxRequest.getCookiesMap(), restxResponse.getStatus(), stopwatch);
        }
    },
    DEFAULT {
        public void log(Logger logger, RestxRequest restxRequest, RestxResponse restxResponse, Stopwatch stopwatch) {
            logger.info("<< {} >> {} - {}", restxRequest, restxResponse.getStatus(), stopwatch);
        }
    },
    QUIET {
        public void log(Logger logger, RestxRequest restxRequest, RestxResponse restxResponse, Stopwatch stopwatch) {
            logger.debug("<< {} >> {} - {}", restxRequest, restxResponse.getStatus(), stopwatch);
        }
    }
    ;

    public abstract void log(Logger logger, RestxRequest restxRequest, RestxResponse restxResponse, Stopwatch stopwatch);
}
