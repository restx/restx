package restx.build;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:21 PM
 */
public class GAV {
    private static final String OPTIONAL_SUFFIX = "!optional";
    public static GAV parse(String gav) {
        String[] parts = gav.split(":");
        if (parts.length < 3 || parts.length > 5) {
            throw new IllegalArgumentException("can't parse '" + gav + "' as a module coordinates (GAV). " +
                    "It must have at least 3 parts separated by columns. (4th and 5th are optional and correspond to artifact type and classifier)");
        }
        boolean optional = false;
        if(parts[parts.length-1].endsWith(OPTIONAL_SUFFIX)) {
            optional = true;
            parts[parts.length-1] = parts[parts.length-1].substring(0, parts[parts.length-1].length()-OPTIONAL_SUFFIX.length());
        }
        if(parts.length == 3) {
            return new GAV(parts[0], parts[1], parts[2], optional);
        }
        if(parts.length == 4) {
        	return new GAV(parts[0], parts[1], parts[2], parts[3], optional);
        }
    	return new GAV(parts[0], parts[1], parts[2], parts[3], parts[4], optional);
    }

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String classifier;
    private final boolean optional;

    public GAV(String groupId, String artifactId, String version, final boolean optional) {
        this(groupId, artifactId, version, null, null, optional);
    }

    public GAV(String groupId, String artifactId, String version, String type, final boolean optional) {
        this(groupId, artifactId, version, type, null, optional);
    }
    
    public GAV(final String groupId, final String artifactId, final String version, final String type, final String classifier, final boolean optional) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.optional = optional;
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

    public boolean isOptional() {
        return optional;
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
        String suffix = optional?OPTIONAL_SUFFIX:"";
        if (type == null){
            return groupId + ":" + artifactId + ":" + version + suffix;
        }
        if(classifier == null) {
            return groupId + ":" + artifactId + ":" + version + ":" + type + suffix;
        }
        return groupId + ":" + artifactId + ":" + version + ":" + type + ":" + classifier + suffix;
    }
}
