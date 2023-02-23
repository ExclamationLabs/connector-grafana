package com.exclamationlabs.connid.base.grafana.model.response;

/**
 * Class to hold the Common JSON response for a grafana request
 */
public class GrafanaStandardResponse
{
    private String id;
    private String message;
    private String orgId;
    private String traceID;
    private String userId;

    public String getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    public String getOrgId()
    {
        return orgId;
    }

    public String getTraceID()
    {
        return traceID;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setMessage(String data)
    {
        this.message= data;
    }

    public void setOrgId(String orgId)
    {
        this.orgId = orgId;
    }

    public void setTraceID(String traceID)
    {
        this.traceID = traceID;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }
}
