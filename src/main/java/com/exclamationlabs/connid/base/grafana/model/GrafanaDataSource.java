package com.exclamationlabs.connid.base.grafana.model;

import com.exclamationlabs.connid.base.connector.model.IdentityModel;

import java.util.Map;

/**
 * Grafana Data Source Model
 */
public class GrafanaDataSource implements IdentityModel
{
    private String access;
    private Boolean basicAuth;
    private String  basicAuthPassword;
    private String  basicAuthUser;
    private transient String  dashboardTemplateName;
    private String database;
    private Integer id;
    private Boolean isDefault;
    private Map     jsonData;
    private String name;
    private Integer orgId;
    private transient String  orgName;
    private String  password;
    private Boolean readOnly;
    private Map     secureJsonData;
    private Map     secureJsonFields;
    private String type;
    private String  typeLogoURL;
    private String  uid;
    private String url;
    private String user;
    private String  version;
    private Boolean withCredentials;

    public String getAccess()
    {
        return access;
    }

    public Boolean getBasicAuth()
    {
        return basicAuth;
    }

    public String getBasicAuthPassword()
    {
        return basicAuthPassword;
    }

    public String getBasicAuthUser()
    {
        return basicAuthUser;
    }

    public String getDashboardTemplateName()
    {
        return dashboardTemplateName;
    }

    public String getDatabase()
    {
        return database;
    }

    public Boolean getDefault()
    {
        return isDefault;
    }

    public Integer getId()
    {
        return id;
    }

    @Override
    public String getIdentityIdValue()
    {
        String identityId = String.format("%d_%s", orgId, uid);
        return identityId;
    }

    @Override
    public String getIdentityNameValue()
    {
        String identityName = String.format("%d_%s", orgId, name);
        return identityName;
    }

    public Map<String, String> getJsonData()
    {
        return jsonData;
    }

    public String getName()
    {
        return name;
    }

    public Integer getOrgId()
    {
        return orgId;
    }

    public String getOrgName()
    {
        return orgName;
    }

    public String getPassword()
    {
        return password;
    }

    public Boolean getReadOnly()
    {
        return readOnly;
    }

    public Map<String, String> getSecureJsonData()
    {
        return secureJsonData;
    }

    public Map getSecureJsonFields()
    {
        return secureJsonFields;
    }

    public String getType()
    {
        return type;
    }

    public String getTypeLogoURL()
    {
        return typeLogoURL;
    }

    public String getUid()
    {
        return uid;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUser()
    {
        return user;
    }

    public String getVersion()
    {
        return version;
    }

    public Boolean getWithCredentials()
    {
        return withCredentials;
    }

    @Override
    public boolean identityEquals(Class<? extends IdentityModel> identityClass, IdentityModel compareSource, Object compareTo)
    {
        return IdentityModel.super.identityEquals(identityClass, compareSource, compareTo);
    }

    @Override
    public int identityHashCode()
    {
        return IdentityModel.super.identityHashCode();
    }

    @Override
    public String identityToString()
    {
        return IdentityModel.super.identityToString();
    }

    public void setAccess(String access)
    {
        this.access = access;
    }

    public void setBasicAuth(Boolean basicAuth)
    {
        this.basicAuth = basicAuth;
    }

    public void setBasicAuthPassword(String basicAuthPassword)
    {
        this.basicAuthPassword = basicAuthPassword;
    }

    public void setBasicAuthUser(String basicAuthUser)
    {
        this.basicAuthUser = basicAuthUser;
    }

    public void setDashboardTemplateName(String dashboardTemplateName)
    {
        this.dashboardTemplateName = dashboardTemplateName;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public void setDefault(Boolean aDefault)
    {
        isDefault = aDefault;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public void setJsonData(Map jsonData)
    {
        this.jsonData = jsonData;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setOrgId(Integer orgId)
    {
        this.orgId = orgId;
    }

    public void setOrgName(String orgName)
    {
        this.orgName = orgName;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setReadOnly(Boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public void setSecureJsonData(Map secureJsonData)
    {
        this.secureJsonData = secureJsonData;
    }

    public void setSecureJsonFields(Map secureJsonFields)
    {
        this.secureJsonFields = secureJsonFields;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setTypeLogoURL(String typeLogoURL)
    {
        this.typeLogoURL = typeLogoURL;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public void setWithCredentials(Boolean withCredentials)
    {
        this.withCredentials = withCredentials;
    }
}
