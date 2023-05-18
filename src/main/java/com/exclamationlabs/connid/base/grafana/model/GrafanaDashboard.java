package com.exclamationlabs.connid.base.grafana.model;


public class GrafanaDashboard
{
    private String dashboard;
    private String meta;

    public String getDashboard()
    {
        return dashboard;
    }

    public String getMeta()
    {
        return meta;
    }

    public void setDashboard(String dashboard)
    {
        this.dashboard = dashboard;
    }

    public void setMeta(String meta)
    {
        this.meta = meta;
    }
}
