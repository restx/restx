package restx.classloader;

import com.google.common.collect.ImmutableCollection;
import org.joda.time.DateTime;

import java.nio.file.Path;

/**
* User: xavierhanin
* Date: 7/26/13
* Time: 11:25 PM
*/
public class CompilationFinishedEvent {
    private final CompilationManager compilationManager;
    private final DateTime endTime;
    private final ImmutableCollection<Path> sources;

    public CompilationFinishedEvent(CompilationManager compilationManager, DateTime endTime, ImmutableCollection<Path> sources) {
        this.compilationManager = compilationManager;
        this.endTime = endTime;
        this.sources = sources;
    }

    public CompilationManager getCompilationManager() {
        return compilationManager;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public ImmutableCollection<Path> getSources() {
        return sources;
    }
}
