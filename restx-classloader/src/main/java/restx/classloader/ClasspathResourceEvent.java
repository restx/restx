package restx.classloader;

/**
 * User: xavierhanin
 * Date: 9/7/13
 * Time: 1:31 PM
 */
public class ClasspathResourceEvent {
    public static enum Kind {
        CREATED, UPDATED
    }

    private final Kind kind;

    private final String resourcePath;

    public ClasspathResourceEvent(Kind kind, String resourcePath) {
        this.kind = kind;
        this.resourcePath = resourcePath;
    }

    public Kind getKind() {
        return kind;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public String toString() {
        return "ClasspathResourceEvent{" +
                "kind=" + kind +
                ", resourcePath=" + resourcePath +
                '}';
    }
}
