package restx.metrics.codahale;

import restx.common.metrics.api.Monitor;
import restx.common.metrics.api.Timer;

public class CodahaleTimer implements Timer {
    com.codahale.metrics.Timer codahaleTimer;

    public CodahaleTimer(com.codahale.metrics.Timer codahaleTimer) {
        this.codahaleTimer = codahaleTimer;
    }

    @Override
    public Monitor time() {
        return new CodahaleMonitor(codahaleTimer.time());
    }

    public com.codahale.metrics.Timer getCodahaleTimer() {
        return codahaleTimer;
    }
}
