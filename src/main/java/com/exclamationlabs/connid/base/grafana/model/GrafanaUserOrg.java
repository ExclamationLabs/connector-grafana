package com.exclamationlabs.connid.base.grafana.model;

/**
 * Simple class to contain the organizations associated with a User
 */
public class GrafanaUserOrg
{
    private String name;
    private int orgId;
    private String role;

    public String getName()
    {
        return name;
    }

    public int getOrgId()
    {
        return orgId;
    }

    public String getRole()
    {
        return role;
    }

    public void setName(String name)
    {
        this.name = name;
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
