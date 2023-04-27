package com.exclamationlabs.connid.base.grafana.model.response;

import com.exclamationlabs.connid.base.grafana.model.GrafanaDataSource;

/**
 * Class to hold the Common JSON response for a grafana request
 */
public class GrafanaDatasourceResponse
{
    private GrafanaDataSource datasource;
    private String id;
    private String message;
    private String name;

    public GrafanaDataSource getDatasource()
    {
        return datasource;
    }

    public String getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    public String getName()
    {
        return name;
    }

    public void setDatasource(GrafanaDataSource datasource)
    {
        this.datasource = datasource;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setMessage(String data)
    {
        this.message= data;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
