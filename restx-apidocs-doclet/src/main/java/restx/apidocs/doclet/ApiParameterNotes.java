package restx.apidocs.doclet;

/**
 * Date: 16/5/14
 * Time: 22:26
 */
public class ApiParameterNotes {
    // parameter id
    private String name;

    private String notes;

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public ApiParameterNotes setName(final String name) {
        this.name = name;
        return this;
    }

    public ApiParameterNotes setNotes(final String notes) {
        this.notes = notes;
        return this;
    }
}
