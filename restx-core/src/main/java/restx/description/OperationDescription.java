package restx.description;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import restx.http.HttpStatus;

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
    public String sourceLocation = "";
    public String summary = "";
    public String notes = "";
    public HttpStatus.Descriptor successStatus;
    public List<OperationParameterDescription> parameters = Lists.newArrayList();
    public List<ErrorResponseDescription> errorResponses = Lists.newArrayList();
    public List<OperationReference> relatedOperations = Lists.newArrayList();

    public Optional<OperationParameterDescription> findBodyParameter() {
        for (OperationParameterDescription parameter : parameters) {
            if (parameter.paramType == OperationParameterDescription.ParamType.body) {
                return Optional.of(parameter);
            }
        }

        return Optional.absent();
    }
}
