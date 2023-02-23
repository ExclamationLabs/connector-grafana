package com.exclamationlabs.connid.base.grafana.model;

/**
 * Simple class to add a user to an organization
 */
public class GrafanaAddUserToOrg
{
    private String loginOrEmail;
    private int orgId;
    private String role;

    public String getLoginOrEmail()
    {
        return loginOrEmail;
    }

    public int getOrgId()
    {
        return orgId;
    }

    public String getRole()
    {
        return role;
    }

    public void setLoginOrEmail(String loginOrEmail)
    {
        this.loginOrEmail = loginOrEmail;
    }

    public void setOrgId(int orgId)
    {
        this.orgId = orgId;
    }

    public void setRole(String role)
    {
        this.role = role;
    }
}
