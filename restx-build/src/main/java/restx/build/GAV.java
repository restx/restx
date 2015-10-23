package restx.build;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:21 PM
 */
public class GAV {
    public static GAV parse(String gav) {
        String[] parts = gav.split(":");
        if (parts.length < 3 || parts.length > 5) {
            throw new IllegalArgumentException("can't parse '" + gav + "' as a module coordinates (GAV). " +
                    "It must have at least 3 parts separated by columns. (4th and 5th are optional and correspond to artifact type and classifier)");
        }
        if(parts.length == 3) {
            return new GAV(parts[0], parts[1], parts[2]);
        }
        if(parts.length == 4) {
        	return new GAV(parts[0], parts[1], parts[2], parts[3]);
        }
    	return new GAV(parts[0], parts[1], parts[2], parts[3], parts[4]);
    }

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String classifier;

    public GAV(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null, null);
    }

    public GAV(String groupId, String artifactId, String version, String type) {
        this(groupId, artifactId, version, type, null);
    }
    
    public GAV(final String groupId, final String artifactId, final String version, final String type, final String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
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

    public String getClassifier() {
    	return classifier;
    }
    
    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    /**
     * toString() will only generate simple GAV whereas toParseableString()
     * should generate a String that should return the same GAV content when parsed
     * through GAV.parse()
     */
    public String toParseableString(){
        if (type == null){
            return groupId + ":" + artifactId + ":" + version;
        }
        if(classifier == null) {
            return groupId + ":" + artifactId + ":" + version + ":" + type;
        }
        return groupId + ":" + artifactId + ":" + version + ":" + type + ":" + classifier;
    }
}
