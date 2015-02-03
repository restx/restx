package samplest.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.AutoStartable;
import restx.factory.Component;

/**
 * Date: 3/2/15
 * Time: 21:56
 */
@Component
public class AutoCloseableSample implements AutoStartable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(AutoCloseableSample.class);

    @Override
    public void start() {
        logger.info("starting {}", this);
    }

    @Override
    public void close() throws Exception {
        logger.info("closing {}", this);
    }
}
