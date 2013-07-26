package restx.classloader;

import org.joda.time.DateTime;

/**
* User: xavierhanin
* Date: 7/26/13
* Time: 11:25 PM
*/
public class CompilationFinishedEvent {
    private final CompilationManager compilationManager;
    private final DateTime endTime;

    public CompilationFinishedEvent(CompilationManager compilationManager, DateTime endTime) {
        this.compilationManager = compilationManager;
        this.endTime = endTime;
    }

    public CompilationManager getCompilationManager() {
        return compilationManager;
    }

    public DateTime getEndTime() {
        return endTime;
    }
}
