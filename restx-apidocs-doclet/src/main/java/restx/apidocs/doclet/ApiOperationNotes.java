package restx.apidocs.doclet;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 16/5/14
 * Time: 22:25
 */
public class ApiOperationNotes {
    // operation id
    private String path;
    private String httpMethod;

    // notes
    private String notes;
    private List<ApiParameterNotes> parameters = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getNotes() {
        return notes;
    }

    public List<ApiParameterNotes> getParameters() {
        return parameters;
    }

    public ApiOperationNotes setPath(final String path) {
        this.path = path;
        return this;
    }

    public ApiOperationNotes setHttpMethod(final String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public ApiOperationNotes setNotes(final String notes) {
        this.notes = notes;
        return this;
    }

    public ApiOperationNotes setParameters(final List<ApiParameterNotes> parameters) {
        this.parameters = parameters;
        return this;
    }
}
