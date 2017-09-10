package samplest.security;

import restx.security.Role;
import restx.security.Roles;

/**
 * Example of using an enum-based class of roles based on companyId+subCompanyId parameters
 * Such enum will provide utility methods to create role string based on their parameters (useful when programmatically assigning those roles)
 * Moreover, it provides Role name constants usable directly in @RolesAllowed annotations
 */
public enum CompanyAndSubCompanyRoles implements Role {

    CAN_EDIT_COMPANY(Constants.CAN_EDIT_COMPANY__$COMPANY_ID__SUBCOMPANY__$SUBCOMPANY_ID__),
    CAN_EDIT_COMPANY_INVOICES(Constants.CAN_EDIT_COMPANY__$COMPANY_ID__SUBCOMPANY__$SUBCOMPANY_ID__INVOICES);

    private String rawName;
    CompanyAndSubCompanyRoles(String rawName) {
        this.rawName = rawName;
    }

    public String getRawName() {
        return rawName;
    }

    public String getFor(String companyId, String subCompanyId){
        return Roles.getInterpolatedRoleName(this.rawName, companyId, subCompanyId);
    }
    public String getEverySubCompaniesForCompany(String companyId) {
        return getFor(companyId, "*");
    }
    public String getForEveryCompanies(){
        return getEverySubCompaniesForCompany("*");
    }

    public static class Constants {
        public static final String CAN_EDIT_COMPANY__$COMPANY_ID__SUBCOMPANY__$SUBCOMPANY_ID__ = "CAN_EDIT_COMPANY_{companyId}_SUBCOMPANY_{subCompanyId}";
        public static final String CAN_EDIT_COMPANY__$COMPANY_ID__SUBCOMPANY__$SUBCOMPANY_ID__INVOICES = "CAN_EDIT_COMPANY_{companyId}_SUBCOMPANY_{subCompanyId}_INVOICES";
    }
}
