package restx.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: xavierhanin
 * Date: 5/4/13
 * Time: 2:47 PM
 */
public class ModuleDescriptor {
    private final String id;
    private final String category;
    private final String description;

    @JsonCreator
    public ModuleDescriptor(@JsonProperty("id") String id,
                            @JsonProperty("category") String category,
                            @JsonProperty("description") String description) {
        this.id = id;
        this.category = category;
        this.description = description;
    }

    @Override
    public String toString() {
        return "ModuleDescriptor{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    /**
     * The module id without revision information
     * @return module id without revision information
     */
    public String getModuleId() {
        return id.substring(0, id.lastIndexOf(":"));
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
