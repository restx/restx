package restx.metrics.codahale;

import com.codahale.metrics.Timer;
import restx.common.metrics.api.Monitor;

public class CodahaleMonitor implements Monitor {

    private Timer.Context codahaleTimerContext;

    public CodahaleMonitor(Timer.Context codahaleTimerContext) {
        this.codahaleTimerContext = codahaleTimerContext;
    }

    @Override
    public long stop() {
        return codahaleTimerContext.stop();
    }

    public Timer.Context getCodahaleTimerContext() {
        return codahaleTimerContext;
    }
}
