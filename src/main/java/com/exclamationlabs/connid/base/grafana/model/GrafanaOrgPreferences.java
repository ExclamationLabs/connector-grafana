package com.exclamationlabs.connid.base.grafana.model;

/**
 * Class to hold the Common JSON response for a grafana dashboard request
 */
public class GrafanaOrgPreferences
{
    private Integer homeDashboardId;
    private String homeDashboardUID;
    private String locale;
    private String theme;
    private String timezone;
    private String weekStart;

    public Integer getHomeDashboardId()
    {
        return homeDashboardId;
    }

    public String getHomeDashboardUID()
    {
        return homeDashboardUID;
    }

    public String getTheme()
    {
        return theme;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public String getWeekStart()
    {
        return weekStart;
    }

    public void setHomeDashboardId(Integer homeDashboardId)
    {
        this.homeDashboardId = homeDashboardId;
    }

    public void setHomeDashboardUID(String homeDashboardUID)
    {
        this.homeDashboardUID = homeDashboardUID;
    }

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public void setWeekStart(String weekStart)
    {
        this.weekStart = weekStart;
    }
}
