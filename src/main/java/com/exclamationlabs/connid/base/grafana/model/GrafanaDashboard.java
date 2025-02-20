package com.exclamationlabs.connid.base.grafana.model;


import com.exclamationlabs.connid.base.connector.model.IdentityModel;

public class GrafanaDashboard implements IdentityModel
{
    private transient String dashboard;
    private transient String dataSourceUid;
    private Integer id;
    private Boolean isStarred;
    private transient String meta;
    private transient Integer orgId;
    private transient String orgName;
    private transient String properties;
    private String slug;
    private String sortMeta;
    private transient String status;
    private String[] tags;
    private transient String templateName;
    private String title;
    private String type;
    private String uid;
    private String uri;
    private String url;

    public String getDashboard()
    {
        return dashboard;
    }

    public String getDataSourceUid()
    {
        return dataSourceUid;
    }

    public Integer getId()
    {
        return id;
    }

    @Override
    public String getIdentityIdValue()
    {
        return String.format("%d_%s", orgId, uid);
    }

    @Override
    public String getIdentityNameValue()
    {
        return String.format("%d_%s", orgId, uid);
    }

    public String getMeta()
    {
        return meta;
    }

    public Integer getOrgId()
    {
        return orgId;
    }

    public String getOrgName()
    {
        return orgName;
    }

    public String getProperties()
    {
        return properties;
    }

    public String getSlug()
    {
        return slug;
    }

    public String getSortMeta()
    {
        return sortMeta;
    }

    public Boolean getStarred()
    {
        return isStarred;
    }

    public String getStatus()
    {
        return status;
    }

    public String[] getTags()
    {
        return tags;
    }

    public String getTemplateName()
    {
        return templateName;
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

    public void setDashboard(String dashboard)
    {
        this.dashboard = dashboard;
    }

    public void setDataSourceUid(String dataSourceUid)
    {
        this.dataSourceUid = dataSourceUid;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public void setMeta(String meta)
    {
        this.meta = meta;
    }

    public void setOrgId(Integer orgId)
    {
        this.orgId = orgId;
    }

    public void setOrgName(String orgName)
    {
        this.orgName = orgName;
    }

    public void setProperties(String properties)
    {
        this.properties = properties;
    }

    public void setSlug(String slug)
    {
        this.slug = slug;
    }

    public void setSortMeta(String sortMeta)
    {
        this.sortMeta = sortMeta;
    }

    public void setStarred(Boolean starred)
    {
        isStarred = starred;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public void setTags(String[] tags)
    {
        this.tags = tags;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
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
