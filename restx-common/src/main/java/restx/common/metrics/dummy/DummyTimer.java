package restx.common.metrics.dummy;

import restx.common.metrics.api.Monitor;
import restx.common.metrics.api.Timer;

public class DummyTimer implements Timer {

    public DummyTimer(String name) {
    }

    @Override
    public Monitor time() {
        return new DummyMonitor();
    }
}
