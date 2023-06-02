package com.exclamationlabs.connid.base.grafana.model;


public class GrafanaHealth
{
    private String commit;
    private String database;
    private String version;

    public String getCommit()
    {
        return commit;
    }

    public String getDatabase()
    {
        return database;
    }

    public String getVersion()
    {
        return version;
    }

    public void setCommit(String commit)
    {
        this.commit = commit;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
