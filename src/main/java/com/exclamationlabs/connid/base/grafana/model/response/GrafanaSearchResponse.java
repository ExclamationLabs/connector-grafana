package com.exclamationlabs.connid.base.grafana.model.response;

/**
 * Class to hold the Common JSON response for a grafana dashboard request
 */
public class GrafanaSearchResponse
{
    private Integer id;
    private String slug;
    private String sortMeta;
    private String title;
    private String type;
    private String uid;
    private String uri;
    private String url;

    public Integer getId()
    {
        return id;
    }

    public String getSlug()
    {
        return slug;
    }

    public String getSortMeta()
    {
        return sortMeta;
    }

    public String getTitle()
    {
        return title;
    }

    public String getType()
    {
        return type;
    }

    public String getUid()
    {
        return uid;
    }

    public String getUri()
    {
        return uri;
    }

    public String getUrl()
    {
        return url;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public void setSlug(String slug)
    {
        this.slug = slug;
    }

    public void setSortMeta(String sortMeta)
    {
        this.sortMeta = sortMeta;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
