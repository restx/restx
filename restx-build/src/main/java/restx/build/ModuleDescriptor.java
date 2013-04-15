package restx.build;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:07 PM
 */
public class ModuleDescriptor {
    private final GAV parent;
    private final GAV gav;
    private final String packaging;
    private final Map<String,String> properties;
    private final Map<String, List<ModuleFragment>> fragments;
    private final Map<String, List<ModuleDependency>> dependencies;

    public ModuleDescriptor(GAV parent, GAV gav, String packaging,
                            Map<String, String> properties,
                            Map<String, List<ModuleFragment>> fragments,
                            Map<String, List<ModuleDependency>> dependencies) {
        this.parent = parent;
        this.gav = gav;
        this.packaging = packaging;
        this.fragments = Collections.unmodifiableMap(fragments);
        this.properties = Collections.unmodifiableMap(properties);
        this.dependencies = Collections.unmodifiableMap(dependencies);
    }

    public GAV getParent() {
        return parent;
    }

    public GAV getGav() {
        return gav;
    }

    public String getPackaging() {
        return packaging;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    public Set<String> getDependencyScopes() {
        return dependencies.keySet();
    }

    public List<ModuleDependency> getDependencies(String scope) {
        return dependencies.get(scope);
    }

    public List<ModuleFragment> getFragments(String s) {
        List<ModuleFragment> moduleFragments = fragments.get(s);
        return moduleFragments == null ? Collections.<ModuleFragment>emptyList() : moduleFragments;
    }
}
