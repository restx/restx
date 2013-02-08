package restx.description;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 10:54 AM
 */
public class OperationDescription {
    public String httpMethod;
    public String nickname;
    public String responseClass;
    public String summary = "";
    public String notes = "";
    public List<OperationParameterDescription> parameters = Lists.newArrayList();
    public List<ErrorResponseDescription> errorResponses = Lists.newArrayList();
}
