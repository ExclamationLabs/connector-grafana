package com.exclamationlabs.connid.base.grafana.model.response;

import com.exclamationlabs.connid.base.grafana.model.GrafanaUser;

import java.util.Set;

/**
 * The Grafana User Search Response. This information is returned in response to a search request
 */
public class GrafanaUserResponse
{
    private Integer page;
    private Integer perPage;
    private Integer totalCount;
    private Set<GrafanaUser> users;

    public Integer getPage()
    {
        return page;
    }

    public Integer getPerPage()
    {
        return perPage;
    }

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public Set<GrafanaUser> getUsers()
    {
        return users;
    }

    public void setPage(Integer page)
    {
        this.page = page;
    }

    public void setPerPage(Integer perPage)
    {
        this.perPage = perPage;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public void setUsers(Set<GrafanaUser> users)
    {
        this.users = users;
    }
}
