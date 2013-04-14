package restx.build;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:21 PM
 */
public class GAV {
    public static GAV parse(String gav) {
        String[] parts = gav.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("can't parse '" + gav + "' as a module coordinates (GAV). " +
                    "It must have exactly 3 parts separated by columns.");
        }
        return new GAV(parts[0], parts[1], parts[2]);
    }

    private final String groupId;
    private final String artifactId;
    private final String version;

    public GAV(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
