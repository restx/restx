package restx.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Roles {

    private static final Pattern ROLE_OID_REGEX = Pattern.compile("\\{[^}]+\\}");

    public static String getInterpolatedRoleName(String rawName, String... oids) {
        StringBuffer interpolatedRoleName = new StringBuffer();
        Matcher matcher = ROLE_OID_REGEX.matcher(rawName);

        int i=0;
        while(matcher.find()){
            matcher.appendReplacement(interpolatedRoleName, oids[i]);
            i++;
        }
        matcher.appendTail(interpolatedRoleName);
        return interpolatedRoleName.toString();
    }

    public static String getInterpolatedRoleNameWithPrefix(String rolePrefix, String rawName, String... oids) {
        return rolePrefix+"::"+getInterpolatedRoleName(rawName, oids);
    }
}
