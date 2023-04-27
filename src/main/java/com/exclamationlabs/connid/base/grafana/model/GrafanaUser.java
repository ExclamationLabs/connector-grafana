package com.exclamationlabs.connid.base.grafana.model;

import com.exclamationlabs.connid.base.connector.model.IdentityModel;
import com.google.gson.annotations.SerializedName;

import java.beans.Transient;
import java.util.List;

public class GrafanaUser implements IdentityModel
{
    private String avatarUrl;
    private String email;
    private Integer id;
    private String isDisabled;
    private String lastSeenAt;
    private String lastSeenAtAge;
    private String login;
    private String name;
    private Integer orgId;
    private transient List<String> organizations;
    private transient List<String> orgsAdd;
    private transient List<String> orgsRemove;
    private String password;
    private String role;
    private Integer userId;

    public String getAvatarUrl()
    {
        return avatarUrl;
    }

    public String getEmail()
    {
        return email;
    }

    public Integer getId()
    {
        Integer theId;
        if ( id  != null )
        {
            theId = id;
        }
        else
        {
            theId = getUserId();
        }
        return theId;
    }

    @Override
    public String getIdentityIdValue()
    {
        String identity = null;
        if ( id > 0 )
        {
            identity = String.valueOf(id);
        }
        else
        {
            identity = String.valueOf(userId);
        }
        return identity;
    }

    @Override
    public String getIdentityNameValue()
    {
        return getLogin();
    }

    public String getIsDisabled()
    {
        return isDisabled;
    }

    public String getLastSeenAt()
    {
        return lastSeenAt;
    }

    public String getLastSeenAtAge()
    {
        return lastSeenAtAge;
    }

    public String getLogin()
    {
        return login;
    }

    public String getName()
    {
        return name;
    }

    public Integer getOrgId()
    {
        return orgId;
    }

    public List<String> getOrganizations()
    {
        return organizations;
    }

    public List<String> getOrgsAdd()
    {
        return orgsAdd;
    }

    public List<String> getOrgsRemove()
    {
        return orgsRemove;
    }

    public String getPassword()
    {
        return password;
    }

    public String getRole()
    {
        return role;
    }

    public Integer getUserId()
    {
        Integer theId;
        if ( userId  != null  )
        {
            theId = userId;
        }
        else
        {
            theId = id;
        }
        return theId;
    }

    public void setAvatarUrl(String avatarUrl)
    {
        this.avatarUrl = avatarUrl;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public void setIsDisabled(String isDisabled)
    {
        this.isDisabled = isDisabled;
    }

    public void setLastSeenAt(String lastSeenAt)
    {
        this.lastSeenAt = lastSeenAt;
    }

    public void setLastSeenAtAge(String lastSeenAtAge)
    {
        this.lastSeenAtAge = lastSeenAtAge;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setOrgId(Integer orgId)
    {
        this.orgId = orgId;
    }

    public void setOrganizations(List<String> organizations)
    {
        this.organizations = organizations;
    }

    public void setOrgsAdd(List<String> orgsAdd)
    {
        this.orgsAdd = orgsAdd;
    }

    public void setOrgsRemove(List<String> orgsRemove)
    {
        this.orgsRemove = orgsRemove;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }
}
