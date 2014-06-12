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
public class JongoUser implements RestxPrincipal {
    @JsonProperty("_id")
    private final String id;

    private final String name;

    private final ImmutableSet<String> principalRoles;

    @JsonCreator
    public JongoUser(@JsonProperty("_id") String id,
                     @JsonProperty("name") String name,
                     @JsonProperty("principalRoles") String... principalRoles) {
        this.id = id;
        this.name = name;
        this.principalRoles = ImmutableSet.copyOf(principalRoles);
    }

    public String getId() {
        return id;
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
