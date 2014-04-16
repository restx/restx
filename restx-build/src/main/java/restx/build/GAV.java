package restx.build;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:21 PM
 */
public class GAV {
    public static GAV parse(String gav) {
        String[] parts = gav.split(":");
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException("can't parse '" + gav + "' as a module coordinates (GAV). " +
                    "It must have at least 3 parts separated by columns. (4th is optional and correspond to artifact type)");
        }
        if(parts.length == 3) {
            return new GAV(parts[0], parts[1], parts[2]);
        }
        return new GAV(parts[0], parts[1], parts[2], parts[3]);
    }

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;

    public GAV(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null);
    }

    public GAV(final String groupId, final String artifactId, final String version, final String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
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

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        if (type == null){
            return groupId + ":" + artifactId + ":" + version;
        }
        return groupId + ":" + artifactId + ":" + version + ":" + type;
    }
}
