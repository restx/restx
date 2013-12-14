package restx.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

/**
 * Date: 14/12/13
 * Time: 16:57
 */
public class StdUser implements RestxPrincipal {
    private final String name;
    private final ImmutableSet<String> roles;

    @JsonCreator
    public StdUser(@JsonProperty("name") String name, @JsonProperty("roles") ImmutableSet<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    @Override
    @JsonProperty("roles")
    public ImmutableSet<String> getPrincipalRoles() {
        return roles;
    }

    @Override
    public String getName() {
        return name;
    }
}
