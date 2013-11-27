package restx.description;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 10:53 AM
 */
public class ResourceDescription {
    public String path;
    public String stdPath;
    public String description = "";

    public List<OperationDescription> operations = Lists.newArrayList();

}
