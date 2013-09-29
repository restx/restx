package restx.classloader;

import com.google.common.base.Predicate;

import java.nio.file.Path;

/**
 */
public interface CompilationSettings {
    int autoCompileCoalescePeriod();
    Predicate<Path> classpathResourceFilter();
}
