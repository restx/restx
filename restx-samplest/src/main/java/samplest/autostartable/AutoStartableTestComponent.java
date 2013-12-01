package samplest.autostartable;

import restx.factory.AutoStartable;
import restx.factory.Component;

/**
* Date: 1/12/13
* Time: 14:21
*/
@Component
public class AutoStartableTestComponent implements AutoStartable, AutoCloseable {
    private static int closed;
    private static int started;
    private static int instanciated;

    private int called;

    public AutoStartableTestComponent() {
        instanciated++;
    }

    public static int getClosed() {
        return closed;
    }

    public static int getStarted() {
        return started;
    }

    public static int getInstanciated() {
        return instanciated;
    }

    public int getCalled() {
        return called;
    }


    @Override
    public void close() throws Exception {
        closed++;
    }

    @Override
    public void start() {
        started++;
    }

    public void call() {
        this.called++;
    }
}
