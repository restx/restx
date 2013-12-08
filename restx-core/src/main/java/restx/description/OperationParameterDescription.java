package restx.description;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 11:23 AM
 */
public class OperationParameterDescription {
    public static enum ParamType {
        body, path, query
    }

    public ParamType paramType;
    public String name;
    public String description;
    public String dataType;
    public String schemaKey;
    public boolean required;
    public boolean allowMultiple;
}
