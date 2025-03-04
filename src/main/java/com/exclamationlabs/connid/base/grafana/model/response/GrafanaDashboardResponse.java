package com.exclamationlabs.connid.base.grafana.model.response;

import com.exclamationlabs.connid.base.grafana.model.GrafanaDataSource;

/**
 * Class to hold the Common JSON response for a grafana create dashboard request
 */
public class GrafanaDashboardResponse
{
    private Integer id;
    private String message;
    private String slug;
    private String status;
    private String uid;
    private String url;
    private String version;

    public Integer getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    public String getSlug()
    {
        return slug;
    }

    public String getStatus()
    {
        return status;
    }

    public String getUid()
    {
        return uid;
    }

    public String getUrl()
    {
        return url;
    }

    public String getVersion()
    {
        return version;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setSlug(String slug)
    {
        this.slug = slug;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
