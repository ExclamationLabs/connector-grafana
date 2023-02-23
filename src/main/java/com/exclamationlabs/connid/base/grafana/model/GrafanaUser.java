package com.exclamationlabs.connid.base.grafana.model;

import com.exclamationlabs.connid.base.connector.model.IdentityModel;
import com.google.gson.annotations.SerializedName;

public class GrafanaUser implements IdentityModel
{
    private String avatarUrl;
    private String email;
    private String isDisabled;
    private String lastSeenAt;
    private String lastSeenAtAge;
    private String login;
    private String name;
    private int orgId;
    private String password;
    private String role;
    private int userId;
    private int id;

    public String getAvatarUrl()
    {
        return avatarUrl;
    }

    public String getEmail()
    {
        return email;
    }

    public int getId()
    {
        int theId;
        if ( id  > 0 )
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

    public int getOrgId()
    {
        return orgId;
    }

    public String getPassword()
    {
        return password;
    }

    public String getRole()
    {
        return role;
    }

    public int getUserId()
    {
        int theId;
        if ( userId  > 0 )
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

    public void setId(int id)
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

    public void setOrgId(int orgId)
    {
        this.orgId = orgId;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }
}
