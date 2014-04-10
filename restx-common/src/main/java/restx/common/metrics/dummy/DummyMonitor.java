package restx.common.metrics.dummy;

import restx.common.metrics.api.Monitor;

public class DummyMonitor implements Monitor {

    @Override
    public long stop() {
        return 0;
    }

}
