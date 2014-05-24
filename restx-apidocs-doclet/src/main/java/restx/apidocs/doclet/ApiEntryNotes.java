package restx.apidocs.doclet;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 16/5/14
 * Time: 22:21
 */
public class ApiEntryNotes {
    private String name;
    private List<ApiOperationNotes> operations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<ApiOperationNotes> getOperations() {
        return operations;
    }

    public ApiEntryNotes setName(final String name) {
        this.name = name;
        return this;
    }

    public ApiEntryNotes setOperations(final List<ApiOperationNotes> operations) {
        this.operations = operations;
        return this;
    }
}
