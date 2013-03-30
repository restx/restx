package restx.description;

import java.util.Collection;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 11:35 AM
 */
public interface DescribableRoute {
    public Collection<ResourceDescription> describe();
}
