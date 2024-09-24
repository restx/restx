package restx.jongo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import restx.security.RestxPrincipal;

/**
 * User: Christophe Labouisse
 * Date: 12/06/2014
 * Time: 11:19
 */
public class JongoUserByName implements RestxPrincipal {
    @JsonProperty("_id")
    private final String name;

    private final ImmutableSet<String> principalRoles;

    @JsonCreator
    public JongoUserByName(@JsonProperty("_id") String name,
                           @JsonProperty("principalRoles") String... principalRoles) {
        this.name = name;
        this.principalRoles = ImmutableSet.copyOf(principalRoles);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableSet<String> getPrincipalRoles() {
        return principalRoles;
    }
}
