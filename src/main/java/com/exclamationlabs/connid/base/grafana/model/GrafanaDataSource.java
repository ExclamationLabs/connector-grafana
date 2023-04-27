package com.exclamationlabs.connid.base.grafana.model;

import com.exclamationlabs.connid.base.connector.model.IdentityModel;

import java.util.Map;

/**
 * Grafana Data Source Model
 */
public class GrafanaDataSource implements IdentityModel
{
    Integer id;
    Integer orgId;
    String user;
    String name;
    String database;
    String access;
    String type;
    String url;
    Boolean isDefault;
    Boolean basicAuth;
    String  basicAuthUser;
    String  basicAuthPassword;
    Map     jsonData;
    Map     secureJsonData;

    public Map getSecureJsonFields()
    {
        return secureJsonFields;
    }

    public void setSecureJsonFields(Map secureJsonFields)
    {
        this.secureJsonFields = secureJsonFields;
    }

    Map     secureJsonFields;
    String  uid;
    String  password;
    Boolean readOnly;
    String  typeLogoURL;
    Boolean withCredentials;
    String  version;

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
