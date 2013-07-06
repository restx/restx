package restx.admin;

import restx.security.RestxPrincipal;

/**
 * User: xavierhanin
 * Date: 7/5/13
 * Time: 8:45 PM
 */
public class Session {
    public static final String SESSION_DEF_KEY = "sessionKey";
    private String key;
    private RestxPrincipal principal;

    public Session(String key, RestxPrincipal principal) {
        this.key = key;
        this.principal = principal;
    }

    public String getKey() {
        return key;
    }

    public RestxPrincipal getPrincipal() {
        return principal;
    }
}
