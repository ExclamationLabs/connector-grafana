package com.exclamationlabs.connid.base.grafana.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrafanaUserOrg that = (GrafanaUserOrg) o;
        return orgId == that.orgId && Objects.equals(name, that.name) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, orgId, role);
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
