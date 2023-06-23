package samplest.validation;


import jakarta.validation.constraints.Size;

public class SubPOJO {
    @Size(min=10)
    String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
