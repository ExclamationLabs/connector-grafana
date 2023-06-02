package com.exclamationlabs.connid.base.grafana.model;

import com.exclamationlabs.connid.base.connector.model.IdentityModel;

import java.util.List;

public class GrafanaOrg implements IdentityModel
{
    private GrafanaOrgAddress address;
    private transient List<String> dashboards;
    private transient Integer homeDashboardId;
    private transient String homeDashboardUID;
    private Integer id;
    private String name;
    private transient String timezone;

    public GrafanaOrgAddress getAddress()
    {
        return address;
    }

    public List<String> getDashboards()
    {
        return dashboards;
    }

    public Integer getHomeDashboardId()
    {
        return homeDashboardId;
    }

    public String getHomeDashboardUID()
    {
        return homeDashboardUID;
    }

    public Integer getId()
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

    public String getName()
    {
        return name;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setAddress(GrafanaOrgAddress address)
    {
        this.address = address;
    }

    public void setDashboards(List<String> dashboards)
    {
        this.dashboards = dashboards;
    }

    public void setHomeDashboardId(Integer homeDashboardId)
    {
        this.homeDashboardId = homeDashboardId;
    }

    public void setHomeDashboardUID(String homeDashboardUID)
    {
        this.homeDashboardUID = homeDashboardUID;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
}
