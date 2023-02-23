package com.exclamationlabs.connid.base.grafana.model;

import com.exclamationlabs.connid.base.connector.model.IdentityModel;

public class GrafanaOrg implements IdentityModel
{
    private GrafanaOrgAddress address;
    private int id;
    private String name;

    public GrafanaOrgAddress getAddress()
    {
        return address;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public String getIdentityIdValue()
    {
        return String.valueOf(id);
    }

    @Override
    public String getIdentityNameValue()
    {
        return getName();
    }

    public java.lang.String getName()
    {
        return name;
    }

    public void setAddress(GrafanaOrgAddress address)
    {
        this.address = address;
    }

    public void setId (int id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
