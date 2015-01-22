package samplest.validation;

import org.hibernate.validator.constraints.Email;
import restx.validation.stereotypes.FormValidations;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class POJO {
    @NotNull(groups={FormValidations.Update.class})
    Long id;
    @NotNull
    @Size(min=10, groups={ValidationResource.MyCustomValidationGroup.class})
    String name;
    @Valid
    SubPOJO subPOJO;
    @Email
    String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubPOJO getSubPOJO() {
        return subPOJO;
    }

    public void setSubPOJO(SubPOJO subPOJO) {
        this.subPOJO = subPOJO;
    }
}
