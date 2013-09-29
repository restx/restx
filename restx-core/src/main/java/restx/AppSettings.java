package restx;

import java.net.URI;

/**
 */
public interface AppSettings {
    String appPackage();
    String targetClasses();
    String targetDependency();
    String sourceRoots();
    String mainSources();
    String mainResources();
}
