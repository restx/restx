package restx.servlet;

/**
 * Associates a package name with a server type.
 *
 * Used to guess server type based on class names found in call stack when performing the guess.
 */
public class RegisteredServerType {
    private final String serverType;
    private final String packageName;

    public RegisteredServerType(String serverType, String packageName) {
        this.serverType = serverType;
        this.packageName = packageName;
    }

    public String getServerType() {
        return serverType;
    }

    public String getPackageName() {
        return packageName;
    }
}
